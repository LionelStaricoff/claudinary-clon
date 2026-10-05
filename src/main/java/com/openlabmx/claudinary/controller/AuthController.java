package com.openlabmx.claudinary.controller;

import com.openlabmx.claudinary.dto.request.UserLoginRequest;
import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.JwtResponse;
import com.openlabmx.claudinary.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody UserLoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Create new user account and return JWT tokens")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        JwtResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody String refreshToken) {
        // Extract the token from the request body (it might be wrapped in quotes)
        String token = refreshToken.replace("\"", "").trim();
        JwtResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate current token and logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if current token is valid")
    public ResponseEntity<Boolean> validateToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about the authenticated user")
    public ResponseEntity<JwtResponse> getCurrentUserInfo() {
        JwtResponse response = JwtResponse.builder()
            .userId(authService.getAuthenticatedUserId())
            .username(authService.getAuthenticatedUser().getUsername())
            .email(authService.getAuthenticatedUser().getEmail())
            .roles(authService.getAuthenticatedUser().getRoles().stream()
                .map(role -> role.getName().name()).toList())
            .build();
        return ResponseEntity.ok(response);
    }
}
