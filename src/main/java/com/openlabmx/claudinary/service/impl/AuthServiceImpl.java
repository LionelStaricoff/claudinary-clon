package com.openlabmx.claudinary.service.impl;

import com.openlabmx.claudinary.dto.request.UserLoginRequest;
import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.JwtResponse;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.exception.BadRequestException;
import com.openlabmx.claudinary.exception.ResourceNotFoundException;
import com.openlabmx.claudinary.exception.UnauthorizedException;
import com.openlabmx.claudinary.repository.UserRepository;
import com.openlabmx.claudinary.security.JwtTokenProvider;
import com.openlabmx.claudinary.service.AuthService;
import com.openlabmx.claudinary.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final Set<String> invalidatedTokens = new HashSet<>();

    @Override
    @Transactional
    public JwtResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
        
        if (user.getIsLocked()) {
            throw new UnauthorizedException("User account is locked. Please contact administrator.");
        }
        
        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is deactivated. Please contact administrator.");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setIsLocked(true);
                user.setLockTime(java.time.LocalDateTime.now());
                userRepository.save(user);
                throw new UnauthorizedException("Too many failed login attempts. Account locked for 15 minutes.");
            }
            
            userRepository.save(user);
            throw new UnauthorizedException("Invalid username/email or password");
        }
        
        // Reset failed login attempts on successful login
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        // Authenticate and generate token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        log.info("User {} logged in successfully", user.getUsername());
        
        return JwtResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
            .refreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationSeconds())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
            .build();
    }

    @Override
    @Transactional
    public JwtResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create user entity and save
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStorageUsed(0L);
        user.setStorageLimit(100L * 1024L * 1024L);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        
        // Save user first
        user = userRepository.save(user);
        
        // Create default project for the user
        userService.createDefaultProject(user);
        
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        log.info("User {} registered successfully", user.getUsername());
        
        return JwtResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
            .refreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationSeconds())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
            .build();
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(String refreshToken) {
        if (isTokenInvalidated(refreshToken)) {
            throw new UnauthorizedException("Refresh token has been invalidated");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!jwtTokenProvider.validateRefreshToken(refreshToken, user)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        // Invalidate old refresh token
        invalidateToken(refreshToken);
        
        log.info("Token refreshed for user: {}", user.getUsername());
        
        return JwtResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
            .refreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationSeconds())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
            .build();
    }

    @Override
    public void logout(String token) {
        // Invalidate the access token
        invalidateToken(token);
        
        // Get refresh token from the token string (assuming it's the access token)
        // In a real implementation, you would need to handle refresh tokens properly
        // For now, we just invalidate the access token
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            log.info("User {} logged out successfully", username);
        }
        
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean validateToken(String token) {
        return !isTokenInvalidated(token) && jwtTokenProvider.validateToken(token);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    @Override
    public User getAuthenticatedUser() {
        return userService.getCurrentUserEntity();
    }

    @Override
    public UUID getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    @Override
    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
        log.debug("Invalidated token: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }
}
