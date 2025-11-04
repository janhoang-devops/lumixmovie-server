package com.project.lumix.service;

import com.project.lumix.dto.request.AdminCreateUserRequest;
import com.project.lumix.dto.request.UserCreateRequest;
import com.project.lumix.dto.request.UserUpdateRequest;
import com.project.lumix.dto.request.RoleUpdateRequest;
import com.project.lumix.dto.response.UserResponse;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.entity.Role;
import com.project.lumix.entity.User;
import com.project.lumix.entity.Movie;
import com.project.lumix.enums.Provider;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.MovieMapper;
import com.project.lumix.mapper.UserMapper;
import com.project.lumix.repository.MovieRepository;
import com.project.lumix.repository.RoleRepository;
import com.project.lumix.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserMapper userMapper;
    private final MovieMapper movieMapper;
    private final PasswordEncoder passwordEncoder;
    private final GmailService gmailService;

    public UserResponse createUser(UserCreateRequest request) {
        if (this.userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = this.userMapper.toUsers(request);
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(30L));

        Role userRole = this.roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        user.setProvider(Provider.LOCAL);
        user.setEnabled(false);

        User userSaved = this.userRepository.save(user);

        log.info("Verification Token for user '{}': {}", userSaved.getUsername(), userSaved.getVerificationToken());
        this.gmailService.sendVerificationEmail(userSaved.getEmail(), userSaved.getVerificationToken());

        return this.userMapper.toUserResponse(userSaved);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = this.checkOwnershipOrAdmin(userId);
        this.userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        }

        return this.userMapper.toUserResponse(this.userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUserByAdmin(AdminCreateUserRequest request) {
        if (this.userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = this.userMapper.toUserFromAdminRequest(request);
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        user.setProvider(Provider.LOCAL);

        HashSet<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> foundRoles = this.roleRepository.findAllById(request.getRoles());
            roles.addAll(foundRoles);
        } else {
            Role defaultRole = this.roleRepository.findByName("USER")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            roles.add(defaultRole);
        }

        user.setRoles(roles);
        user.setTokenExpiryDate(null);
        user.setVerificationToken(null);

        return this.userMapper.toUserResponse(this.userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUserRoles(String userId, RoleUpdateRequest request) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if ("admin".equals(user.getUsername())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<Role> roles = this.roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return this.userMapper.toUserResponse(this.userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(String userId) {
        User userToDelete = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if ("admin".equals(userToDelete.getUsername())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        this.userRepository.delete(userToDelete);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUser() {
        return this.userRepository.findAll()
                .stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getMyInfo(String userId) {
        User user = this.checkOwnershipOrAdmin(userId);
        return this.userMapper.toUserResponse(user);
    }

    public void addMovieToFavorites(String userId, String movieId) {
        User user = this.checkOwnershipOrAdmin(userId);
        Movie movie = this.movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        user.getFavoriteMovies().add(movie);
        this.userRepository.save(user);
    }

    public void removeMovieFromFavorites(String userId, String movieId) {
        User user = this.checkOwnershipOrAdmin(userId);
        Movie movie = this.movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        user.getFavoriteMovies().remove(movie);
        this.userRepository.save(user);
    }

    public List<MovieDetailResponse> getFavoriteMovies(String userId) {
        User user = this.checkOwnershipOrAdmin(userId);
        return user.getFavoriteMovies()
                .stream()
                .map(this.movieMapper::toMovieDetailResponse)
                .collect(Collectors.toList());
    }

    public void verifyAccount(String token) {
        User user = this.userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (user.getTokenExpiryDate() == null || user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);

        this.userRepository.save(user);
    }

    private User checkOwnershipOrAdmin(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return user;
    }
}
