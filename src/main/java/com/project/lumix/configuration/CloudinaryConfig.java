package com.project.lumix.configuration;

import com.cloudinary.Cloudinary;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );
        return new Cloudinary(config);
    }

    @PostConstruct
    public void logCloudinaryInit() {
        log.info("✅ Cloudinary initialized with cloud name: {}", cloudName);
    }
}
