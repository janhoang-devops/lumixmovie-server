package com.project.lumix.configuration;
import java.util.Set;

import com.project.lumix.entity.Role;
import com.project.lumix.entity.User;
import com.project.lumix.enums.Provider;
import com.project.lumix.repository.RoleRepository;
import com.project.lumix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = {"datasource.driver-class-name"},
            havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Application running ....");

        return args -> {
            Role userRole = roleRepository.findById("USER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("USER")
                            .description("User role")
                            .build()));

            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ADMIN")
                            .description("Admin role")
                            .build()));

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User adminUser = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .email(adminEmail)
                        .provider(Provider.LOCAL)
                        .enabled(true)
                        .roles(Set.of(adminRole))
                        .build();

                userRepository.save(adminUser);
                log.warn("Default admin user has been created. Please change the password immediately!");
            }
        };
    }
}

