package com.openlabmx.claudinary.controller;

import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.UserResponse;
import com.openlabmx.claudinary.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get current user profile", description = "Get the profile of the authenticated user")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Update current user profile", description = "Update the profile of the authenticated user")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get user by ID", description = "Get user information by ID (Admin only)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get user by username", description = "Get user information by username (Admin only)")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users (Admin only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject Pageable pageable) {
        Page<UserResponse> responses = userService.getAllUsers(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Search users", description = "Search users by query (Admin only)")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<UserResponse> responses = userService.searchUsers(query);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Update user", description = "Update user information (Admin only)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId, 
            @Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user by ID (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Lock user account", description = "Lock user account (Admin only)")
    public ResponseEntity<Void> lockUser(@PathVariable UUID userId) {
        userService.lockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Unlock user account", description = "Unlock user account (Admin only)")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID userId) {
        userService.unlockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Add role to user", description = "Add a role to user (Admin only)")
    public ResponseEntity<Void> addRoleToUser(
            @PathVariable UUID userId, 
            @PathVariable String roleName) {
        userService.addRoleToUser(userId, roleName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Remove role from user", description = "Remove a role from user (Admin only)")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable UUID userId, 
            @PathVariable String roleName) {
        userService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get all admin users", description = "Get list of all admin users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllAdmins() {
        List<UserResponse> responses = userService.getAllAdmins();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/exists/username/{username}")
    @Operation(summary = "Check if username exists", description = "Check if a username already exists")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "Check if email exists", description = "Check if an email already exists")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
