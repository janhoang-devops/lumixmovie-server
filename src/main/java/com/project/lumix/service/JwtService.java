package com.project.lumix.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.lumix.dto.request.IntrospectRequest;
import com.project.lumix.dto.response.IntrospectResponse;
import com.project.lumix.entity.InvalidatedToken;
import com.project.lumix.entity.User;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.repository.InvalidatedTokenRepository;
import com.project.lumix.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESH_DURATION;

    @Value("${cookie.secure}")
    private boolean COOKIE_SECURE;

    @Value("${app.base-url}")
    private String BASE_URL;

    private static final String ACCESS_COOKIE_NAME = "accessToken";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    // Sinh SignedJWT với duration
    public String generateToken(User user, long durationInSeconds) {
        try {
            Instant now = Instant.now();
            Date iat = Date.from(now);
            Date exp = Date.from(now.plusSeconds(durationInSeconds));

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(BASE_URL)
                    .issueTime(iat)
                    .expirationTime(exp)
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", buildScope(user))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            MACSigner signer = new MACSigner(SIGNER_KEY.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, VALID_DURATION);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_DURATION);
    }

    // Verify token và trả về SignedJWT
    public SignedJWT verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            if (jti != null && invalidatedTokenRepository.existsById(jti)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return signedJWT;
        } catch (ParseException | JOSEException e) {
            log.error("Token verify failed", e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    // Logout: invalidate token + xóa cookie
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractCookieValue(request, ACCESS_COOKIE_NAME);
        if (token != null) {
            try {
                SignedJWT jwt = verifyToken(token);
                String jti = jwt.getJWTClaimsSet().getJWTID();
                Date expiryTime = jwt.getJWTClaimsSet().getExpirationTime();
                InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                        .id(jti)
                        .expiryDate(expiryTime)
                        .build();
                invalidatedTokenRepository.save(invalidatedToken);
            } catch (Exception e) {
                log.warn("Cannot invalidate token", e);
            }
        }

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(COOKIE_SECURE)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(COOKIE_SECURE)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    // Refresh token
    public void refresh(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String refreshToken = extractCookieValue(request, REFRESH_COOKIE_NAME);
        if (refreshToken == null) {
            log.warn("Refresh token not found in cookies");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        try {
            SignedJWT signedJWT = verifyToken(refreshToken);

            // invalidate the used refresh token
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jti).expiryDate(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);

            // find user
            String username = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            // generate new tokens
            String newAccessToken = generateToken(user, VALID_DURATION);
            String newRefreshToken = generateToken(user, REFRESH_DURATION);

            // set cookies (tên consistent)
            ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_COOKIE_NAME, newAccessToken)
                    .httpOnly(true)
                    .secure(COOKIE_SECURE)
                    .path("/")
                    .maxAge(Duration.ofSeconds(VALID_DURATION))
                    .sameSite("None")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, newRefreshToken)
                    .httpOnly(true)
                    .secure(COOKIE_SECURE)
                    .path("/")
                    .maxAge(Duration.ofSeconds(REFRESH_DURATION))
                    .sameSite("None")
                    .build();

            httpServletResponse.addHeader("Set-Cookie", accessTokenCookie.toString());
            httpServletResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());

            log.info("Successfully refreshed token for user: {}", username);
        } catch (AppException ex) {
            log.error("Failed to refresh token.", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to refresh token (parse/verify error).", ex);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    // Introspect
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = true;

        try {
            this.verifyToken(token);
        } catch (AppException var5) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    // Helper: lấy scope từ user
    private String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                if (role.getName() != null) joiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(p -> {
                        if (p.getName() != null) joiner.add(p.getName());
                    });
                }
            });
        }
        return joiner.toString();
    }

    // Helper: lấy cookie theo tên
    private String extractCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }
}
