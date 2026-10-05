package com.openlabmx.claudinary.service;

import com.openlabmx.claudinary.dto.request.ProjectRequest;
import com.openlabmx.claudinary.dto.response.ProjectResponse;
import com.openlabmx.claudinary.entity.Project;
import com.openlabmx.claudinary.entity.User;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse getProjectById(UUID projectId);
    List<ProjectResponse> getProjectsByUserId(UUID userId);
    List<ProjectResponse> getCurrentUserProjects();
    ProjectResponse updateProject(UUID projectId, ProjectRequest request);
    void deleteProject(UUID projectId);
    void deleteProjectByUser(UUID userId, UUID projectId);
    
    Project getProjectEntityById(UUID projectId);
    Project getDefaultProjectForUser(UUID userId);
    ProjectResponse getDefaultProjectForCurrentUserAsResponse();
    Project getDefaultProjectForCurrentUser();
    
    List<ProjectResponse> searchProjectsByUser(UUID userId, String query);
    
    boolean existsByUserIdAndName(UUID userId, String name);
    
    Integer countProjectsByUserId(UUID userId);
    
    void createDefaultProject(User user);
}
