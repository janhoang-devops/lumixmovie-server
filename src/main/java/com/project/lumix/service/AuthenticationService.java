package com.project.lumix.service;

import com.project.lumix.dto.request.AuthenticationRequest;
import com.project.lumix.entity.User;
import com.project.lumix.enums.Provider;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User authenticateAndGetUser(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        if (user.getProvider() == Provider.LOCAL &&
                !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return user;
    }
}
