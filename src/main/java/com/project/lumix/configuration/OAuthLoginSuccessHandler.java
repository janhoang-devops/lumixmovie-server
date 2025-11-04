package com.project.lumix.configuration;

import com.project.lumix.entity.Role;
import com.project.lumix.entity.User;
import com.project.lumix.enums.Provider;
import com.project.lumix.repository.RoleRepository;
import com.project.lumix.repository.UserRepository;
import com.project.lumix.service.AuthenticationService;
import com.project.lumix.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Value("${app.oauth2.redirect-url.success}")
    private String successRedirectUrl;

    @Value("${app.oauth2.redirect-url.error}")
    private String errorRedirectUrl;

    @Value("${jwt.valid-duration}")
    private long jwtValidDuration;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttribute("email");

            if (email == null || email.isEmpty()) {
                log.error("Email is null or empty");
                response.sendRedirect(buildErrorRedirectUrl("EMAIL_NOT_PROVIDED"));
                return;
            }

            Optional<User> userOptional = userRepository.findByEmail(email);
            log.info("check user exists: {}", userOptional.isPresent());

            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
                log.info("User already exists via OAuth2: {}, provider: {}", email, user.getProvider());

                if (user.getProvider() != Provider.GOOGLE) {
                    user.setProvider(Provider.GOOGLE);
                    log.info("Linked Google account to existing account: {}", email);
                }
            } else {
                user = createNewUser(oAuth2User);
            }

            // Tạo JWT
            String jwtToken = jwtService.generateToken(user, jwtValidDuration);

            // Cookie
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", jwtToken)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(Duration.ofSeconds(jwtValidDuration))
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());

            // Redirect URL
            String encodedUsername = Base64.getEncoder()
                    .encodeToString(user.getUsername().getBytes(StandardCharsets.UTF_8));

            String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                    .queryParam("login_success", true)
                    .queryParam("userId", user.getUserId())
                    .queryParam("username_b64", encodedUsername)
                    .build()
                    .toUriString();

            log.info("Successfully authenticated user via OAuth2: {}. Redirecting to: {}", email, redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Failed to authenticate user via OAuth2", e);
            response.sendRedirect(buildErrorRedirectUrl("INTERNAL_SERVER_ERROR"));
        }
    }

    private User createNewUser(OAuth2User oAuth2User) {
        String email = (String) oAuth2User.getAttribute("email");
        String name = (String) oAuth2User.getAttribute("name");

        User newUser = new User();
        newUser.setEmail(email);

        // set username
        if (name != null && !name.isEmpty()) {
            String baseUsername = name.replaceAll("\\s+", "");
            if (userRepository.existsByUsername(baseUsername)) {
                newUser.setUsername(baseUsername + "_" + UUID.randomUUID().toString().substring(0, 4));
            } else {
                newUser.setUsername(baseUsername);
            }
        } else {
            String baseUsername = email.split("@")[0];
            newUser.setUsername(baseUsername + "_" + UUID.randomUUID().toString().substring(0, 4));
        }

        newUser.setProvider(Provider.GOOGLE);
        newUser.setPassword("OAUTH2_USER_NO_PASSWORD");

        Set<Role> roles = new HashSet<>();
        roleRepository.findById("USER").ifPresent(roles::add);
        newUser.setRoles(roles);

        newUser.setEnabled(true);

        log.info("Creating new user via OAuth2 for email: {}", email);
        return userRepository.save(newUser);
    }

    private String buildErrorRedirectUrl(String errorCode) {
        return UriComponentsBuilder.fromUriString(errorRedirectUrl)
                .queryParam("errorCode", errorCode)
                .build()
                .toUriString();
    }
}

