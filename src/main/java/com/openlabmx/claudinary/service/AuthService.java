package com.openlabmx.claudinary.service;

import com.openlabmx.claudinary.dto.request.UserLoginRequest;
import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.JwtResponse;
import com.openlabmx.claudinary.entity.User;

import java.util.UUID;

public interface AuthService {
    
    JwtResponse login(UserLoginRequest request);
    JwtResponse register(UserRegisterRequest request);
    JwtResponse refreshToken(String refreshToken);
    void logout(String token);
    
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    UUID getUserIdFromToken(String token);
    
    User getAuthenticatedUser();
    UUID getAuthenticatedUserId();
    
    void invalidateToken(String token);
    boolean isTokenInvalidated(String token);
}
