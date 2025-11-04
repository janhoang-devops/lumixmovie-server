package com.project.lumix.controller;
import com.nimbusds.jose.JOSEException;
import com.project.lumix.dto.request.AuthenticationRequest;
import com.project.lumix.dto.request.IntrospectRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.AuthenticationResponse;
import com.project.lumix.dto.response.IntrospectResponse;
import com.project.lumix.entity.Role;
import com.project.lumix.entity.User;
import com.project.lumix.service.AuthenticationService;
import com.project.lumix.service.JwtService;
import com.project.lumix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final JwtService jwtService;

    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private long REFRESH_DURATION;

    @Value("${app.frontend.registration-result-url}")
    private String FRONTEND_REGISTRATION_URL;

    @Value("${cookie.secure}")
    private boolean COOKIE_SECURE;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse httpServletResponse) {

        log.info("Login request for username: {}", request.getUsername());

        User user = authenticationService.authenticateAndGetUser(request);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(COOKIE_SECURE)
                .path("/")
                .maxAge(Duration.ofSeconds(VALID_DURATION))
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(COOKIE_SECURE)
                .path("/")
                .maxAge(Duration.ofSeconds(REFRESH_DURATION))
                .sameSite("None")
                .build();

        httpServletResponse.addHeader("Set-Cookie", refreshTokenCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", accessTokenCookie.toString());
        AuthenticationResponse responseData = AuthenticationResponse.builder()
                .authenticated(true)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();

        return ApiResponse.<AuthenticationResponse>builder()
                .message("Authentication Successful")
                .result(responseData)
                .build();
    }

    // ===== INTROSPECT =====
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        log.info("Introspect token");
        IntrospectResponse result = jwtService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    // ===== REFRESH =====
    @PostMapping("/refresh")
    public ApiResponse<Void> refresh(HttpServletRequest request, HttpServletResponse httpServletResponse)
            throws JOSEException, ParseException {
        log.info("Refresh token request");
        jwtService.refresh(request, httpServletResponse);
        return ApiResponse.<Void>builder()
                .message("Token Refresh Successful")
                .build();
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse httpServletResponse)
            throws JOSEException, ParseException {
        log.info("Logout request");
        jwtService.logout(request, httpServletResponse);
        return ApiResponse.<Void>builder()
                .message("Logout Successful")
                .build();
    }

    // ===== VERIFY ACCOUNT =====
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyAccount(@RequestParam("token") String token) {
        log.info("Verify account request with token: {}", token);

        try {
            userService.verifyAccount(token);
            URI redirectUri = URI.create(FRONTEND_REGISTRATION_URL + "?status=success");
            return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
        } catch (Exception e) {
            log.error("Account verification failed for token: {}", token, e);
            URI redirectUri = URI.create(FRONTEND_REGISTRATION_URL + "?status=error");
            return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
        }
    }
}

