package com.openlabmx.claudinary.controller;

import com.openlabmx.claudinary.dto.request.ProjectRequest;
import com.openlabmx.claudinary.dto.response.ProjectResponse;
import com.openlabmx.claudinary.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management API endpoints")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Create new project", description = "Create a new project for the authenticated user")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get project by ID", description = "Get project information by ID")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID projectId) {
        ProjectResponse response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get current user projects", description = "Get all projects for the authenticated user")
    public ResponseEntity<List<ProjectResponse>> getCurrentUserProjects() {
        List<ProjectResponse> responses = projectService.getCurrentUserProjects();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Get projects by user ID", description = "Get all projects for a specific user (Admin only)")
    public ResponseEntity<List<ProjectResponse>> getProjectsByUserId(@PathVariable UUID userId) {
        List<ProjectResponse> responses = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Update project", description = "Update project information")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId, 
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Delete project", description = "Delete project by ID")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Search projects by user", description = "Search projects for a specific user (Admin only)")
    public ResponseEntity<List<ProjectResponse>> searchProjectsByUser(
            @PathVariable UUID userId, 
            @RequestParam String query) {
        List<ProjectResponse> responses = projectService.searchProjectsByUser(userId, query);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Search current user projects", description = "Search projects for the authenticated user")
    public ResponseEntity<List<ProjectResponse>> searchCurrentUserProjects(@RequestParam String query) {
        UUID userId = projectService.getDefaultProjectForCurrentUserAsResponse().getUserId();
        List<ProjectResponse> responses = projectService.searchProjectsByUser(userId, query);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Count projects by user", description = "Get the count of projects for a specific user (Admin only)")
    public ResponseEntity<Integer> countProjectsByUserId(@PathVariable UUID userId) {
        Integer count = projectService.countProjectsByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/user/{userId}/name/{name}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Check if project exists", description = "Check if a project with the given name exists for a user")
    public ResponseEntity<Boolean> existsByUserIdAndName(
            @PathVariable UUID userId, 
            @PathVariable String name) {
        boolean exists = projectService.existsByUserIdAndName(userId, name);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/default")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get default project", description = "Get the default project for the authenticated user")
    public ResponseEntity<ProjectResponse> getDefaultProject() {
        ProjectResponse response = projectService.getDefaultProjectForCurrentUserAsResponse();
        return ResponseEntity.ok(response);
    }
}
