package com.openlabmx.claudinary.service.impl;

import com.openlabmx.claudinary.dto.request.ProjectRequest;
import com.openlabmx.claudinary.dto.response.ProjectResponse;
import com.openlabmx.claudinary.entity.Project;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.exception.BadRequestException;
import com.openlabmx.claudinary.exception.ResourceNotFoundException;
import com.openlabmx.claudinary.repository.ProjectRepository;
import com.openlabmx.claudinary.repository.UserRepository;
import com.openlabmx.claudinary.service.ProjectService;
import com.openlabmx.claudinary.service.UserService;
import com.openlabmx.claudinary.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User user = userService.getCurrentUserEntity();
        
        if (projectRepository.existsByUserIdAndName(user.getId(), request.getName())) {
            throw new BadRequestException("Project with name '" + request.getName() + "' already exists for this user");
        }
        
        Project project = modelMapper.map(request, Project.class);
        project.setUser(user);
        project.setIsDefault(false);
        project.setImageCount(0);
        
        project = projectRepository.save(project);
        
        log.info("Created new project: {} for user: {}", project.getName(), user.getUsername());
        
        return modelMapper.map(project, ProjectResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        return modelMapper.map(project, ProjectResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(UUID userId) {
        List<Project> projects = projectRepository.findByUserId(userId);
        return projects.stream()
            .map(project -> modelMapper.map(project, ProjectResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getCurrentUserProjects() {
        User user = userService.getCurrentUserEntity();
        return getProjectsByUserId(user.getId());
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getName().equals(request.getName()) &&
            projectRepository.existsByUserIdAndName(project.getUser().getId(), request.getName())) {
            throw new BadRequestException("Project with name '" + request.getName() + "' already exists for this user");
        }
        
        modelMapper.map(request, project);
        
        project = projectRepository.save(project);
        
        log.info("Updated project: {}", project.getName());
        
        return modelMapper.map(project, ProjectResponse.class);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        projectRepository.delete(project);
        
        log.info("Deleted project: {}", project.getName());
    }

    @Override
    @Transactional
    public void deleteProjectByUser(UUID userId, UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        
        projectRepository.deleteByUserIdAndId(userId, projectId);
        
        log.info("Deleted project {} for user: {}", projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectEntityById(UUID projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public Project getDefaultProjectForUser(UUID userId) {
        return projectRepository.findByUserIdAndIsDefaultTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Default project not found for user: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getDefaultProjectForCurrentUserAsResponse() {
        Project project = getDefaultProjectForCurrentUser();
        return modelMapper.map(project, ProjectResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getDefaultProjectForCurrentUser() {
        User user = userService.getCurrentUserEntity();
        return getDefaultProjectForUser(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjectsByUser(UUID userId, String query) {
        if (query == null || query.isBlank()) {
            return getProjectsByUserId(userId);
        }
        
        return projectRepository.searchByUserIdAndQuery(userId, query).stream()
            .map(project -> modelMapper.map(project, ProjectResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndName(UUID userId, String name) {
        return projectRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countProjectsByUserId(UUID userId) {
        return projectRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public void createDefaultProject(User user) {
        if (projectRepository.existsByUserIdAndIsDefaultTrue(user.getId())) {
            return; // Default project already exists
        }
        
        Project defaultProject = Project.builder()
            .name("Default Project")
            .description("Your default project for images")
            .isDefault(true)
            .isPublic(false)
            .imageCount(0)
            .user(user)
            .build();
        
        projectRepository.save(defaultProject);
        
        log.info("Created default project for user: {}", user.getUsername());
    }
}
