package com.openlabmx.claudinary.service;

import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.UserResponse;
import com.openlabmx.claudinary.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    
    UserResponse createUser(UserRegisterRequest request);
    UserResponse getUserById(UUID userId);
    UserResponse getUserByUsername(String username);
    UserResponse getCurrentUser();
    Page<UserResponse> getAllUsers(Pageable pageable);
    List<UserResponse> searchUsers(String query);
    UserResponse updateUser(UUID userId, UserRegisterRequest request);
    UserResponse updateCurrentUser(UserRegisterRequest request);
    void deleteUser(UUID userId);
    void lockUser(UUID userId);
    void unlockUser(UUID userId);
    void addRoleToUser(UUID userId, String roleName);
    void removeRoleFromUser(UUID userId, String roleName);
    
    User getUserEntityById(UUID userId);
    User getUserEntityByUsername(String username);
    User getCurrentUserEntity();
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    void updateStorageUsed(UUID userId, long storageUsed);
    
    List<UserResponse> getAllAdmins();
    
    void createDefaultProject(User user);
}
