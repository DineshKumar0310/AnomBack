package com.anonboard.service;

import com.anonboard.dto.request.LoginRequest;
import com.anonboard.dto.request.SignupRequest;
import com.anonboard.dto.response.AuthResponse;
import com.anonboard.dto.response.UserResponse;
import com.anonboard.exception.BadRequestException;
import com.anonboard.exception.ForbiddenException;
import com.anonboard.model.User;
import com.anonboard.repository.UserRepository;
import com.anonboard.security.JwtTokenProvider;
import com.anonboard.util.AnonymousNameGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AnonymousNameGenerator nameGenerator;

    @Value("${app.free-user.post-limit:5}")
    private int freePostLimit;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
            AnonymousNameGenerator nameGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.nameGenerator = nameGenerator;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        String anonymousName = nameGenerator.generateUniqueName();

        String avatar = request.getAvatar();
        if (avatar == null || !avatar.matches("avatar_(0[1-9]|1[0-9]|20)")) {
            avatar = "avatar_01";
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .anonymousName(anonymousName)
                .avatar(avatar)
                .role(User.Role.USER)
                .userType(User.UserType.FREE)
                .build();

        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .anonymousName(anonymousName)
                .avatar(avatar)
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // First check if user exists and is banned
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Check if user is banned
        if (user.isBanned()) {
            // Check if ban has expired
            if (user.getBannedUntil() != null && Instant.now().isAfter(user.getBannedUntil())) {
                // Ban expired, unban user
                user.setBanned(false);
                user.setBanReason(null);
                user.setBannedUntil(null);
                userRepository.save(user);
            } else {
                String message = "Your account has been banned";
                if (user.getBanReason() != null) {
                    message += ": " + user.getBanReason();
                }
                throw new ForbiddenException(message);
            }
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .anonymousName(user.getAnonymousName())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // Check if banned
        if (user.isBanned()) {
            throw new ForbiddenException("Your account has been banned");
        }

        int postsRemaining;
        if (user.isPremium()) {
            postsRemaining = -1; // Unlimited for premium
        } else {
            postsRemaining = Math.max(0, freePostLimit - user.getTotalPosts());
        }

        return UserResponse.builder()
                .anonymousName(user.getAnonymousName())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .userType(user.getUserType().name())
                .isPremium(user.isPremium())
                .totalPosts(user.getTotalPosts())
                .postsRemaining(postsRemaining)
                .freePostLimit(freePostLimit)
                .build();
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // Check if banned
        if (user.isBanned()) {
            if (user.getBannedUntil() != null && Instant.now().isAfter(user.getBannedUntil())) {
                user.setBanned(false);
                user.setBanReason(null);
                user.setBannedUntil(null);
                userRepository.save(user);
            } else {
                throw new ForbiddenException("Your account has been banned");
            }
        }

        return user;
    }
}
