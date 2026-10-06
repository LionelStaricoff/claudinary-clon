package com.openlabmx.claudinary.service.impl;

import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.UserResponse;
import com.openlabmx.claudinary.entity.Role;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.exception.BadRequestException;
import com.openlabmx.claudinary.exception.ResourceNotFoundException;
import com.openlabmx.claudinary.repository.RoleRepository;
import com.openlabmx.claudinary.repository.UserRepository;
import com.openlabmx.claudinary.service.ProjectService;
import com.openlabmx.claudinary.service.UserService;
import com.openlabmx.claudinary.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    
    @Lazy
    @Autowired
    private ProjectService projectService;

    @Override
    @Transactional
    public UserResponse createUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Role userRole = roleRepository.findByName(com.openlabmx.claudinary.enums.RoleType.ROLE_USER)
            .orElseThrow(() -> new ResourceNotFoundException("User role not found"));
        
        user.addRole(userRole);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setStorageUsed(0L);
        
        user = userRepository.save(user);
        
        // Create default project for the user
        projectService.createDefaultProject(user);
        
        log.info("Created new user: {}", user.getUsername());
        
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> modelMapper.map(user, UserResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
        }
        
        return userRepository.findAll().stream()
            .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                          user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                          (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                          (user.getLastName() != null && user.getLastName().toLowerCase().contains(query.toLowerCase())))
            .map(user -> modelMapper.map(user, UserResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserRegisterRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        modelMapper.map(request, user);
        
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        user = userRepository.save(user);
        
        log.info("Updated user: {}", user.getUsername());
        
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(UserRegisterRequest request) {
        User user = getCurrentUserEntity();
        return updateUser(user.getId(), request);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Cleanup storage
        fileStorageService.cleanupEmptyDirectories(userId);
        
        userRepository.delete(user);
        
        log.info("Deleted user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void lockUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setIsLocked(true);
        user.setLockTime(java.time.LocalDateTime.now());
        
        userRepository.save(user);
        
        log.info("Locked user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void unlockUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setIsLocked(false);
        user.setLockTime(null);
        user.setFailedLoginAttempts(0);
        
        userRepository.save(user);
        
        log.info("Unlocked user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void addRoleToUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Role role = roleRepository.findByName(com.openlabmx.claudinary.enums.RoleType.valueOf(roleName))
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        if (!user.getRoles().contains(role)) {
            user.addRole(role);
            userRepository.save(user);
            log.info("Added role {} to user: {}", roleName, user.getUsername());
        }
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Role role = roleRepository.findByName(com.openlabmx.claudinary.enums.RoleType.valueOf(roleName))
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        if (user.getRoles().contains(role)) {
            user.removeRole(role);
            userRepository.save(user);
            log.info("Removed role {} from user: {}", roleName, user.getUsername());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return getUserEntityByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void updateStorageUsed(UUID userId, long storageUsed) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setStorageUsed(storageUsed);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllAdmins() {
        return userRepository.findAllAdmins().stream()
            .map(user -> modelMapper.map(user, UserResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createDefaultProject(User user) {
        projectService.createDefaultProject(user);
    }
}
