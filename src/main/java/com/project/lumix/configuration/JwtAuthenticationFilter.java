package com.project.lumix.configuration;

import com.project.lumix.dto.request.IntrospectRequest;
import com.project.lumix.dto.response.IntrospectResponse;
import com.project.lumix.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromCookie(request);
        log.info("Token:{}",token);

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                IntrospectResponse introspectResponse =
                        jwtService.introspect(IntrospectRequest.builder().token(token).build());

                if (introspectResponse.isValid()) {
                    SignedJWT signedJWT = SignedJWT.parse(token);
                    String username = signedJWT.getJWTClaimsSet().getSubject();
                    String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");

                    List<String> authorities = new ArrayList<>();
                    if (scope != null && !scope.isEmpty()) {
                        authorities = Arrays.asList(scope.split(" "));
                    }

                    UserDetails userDetails = User.withUsername(username)
                            .password("") // password bỏ trống vì JWT đã xác thực
                            .authorities(authorities.toArray(new String[0]))
                            .build();

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("✅ Authenticated user: {}", username);
                } else {
                    log.warn("❌ Token không hợp lệ hoặc bị revoke");
                }
            } catch (ParseException e) {
                log.error("❌ Error parsing or validating JWT token", e);
            } catch (Exception e) {
                log.error("❌ Unexpected error during token validation", e);
            }
        } else {
            log.warn("❌ Missing or invalid token on request: {}", request.getRequestURL());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
