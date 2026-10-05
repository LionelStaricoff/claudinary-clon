package com.openlabmx.claudinary.controller;

import com.openlabmx.claudinary.dto.request.ProjectRequest;
import com.openlabmx.claudinary.dto.request.UserLoginRequest;
import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.ImageResponse;
import com.openlabmx.claudinary.dto.response.ProjectResponse;
import com.openlabmx.claudinary.dto.response.UserResponse;
import com.openlabmx.claudinary.service.AuthService;
import com.openlabmx.claudinary.service.ImageService;
import com.openlabmx.claudinary.service.ProjectService;
import com.openlabmx.claudinary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final UserService userService;
    private final AuthService authService;
    private final ProjectService projectService;
    private final ImageService imageService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            
            // Get recent images for authenticated user
            List<ImageResponse> recentImages = imageService.getRecentImagesByUserId(currentUser.getId(), 6);
            model.addAttribute("recentImages", recentImages);
            
            // Get user's projects
            List<ProjectResponse> projects = projectService.getCurrentUserProjects();
            model.addAttribute("projects", projects);
            
            // Get storage info
            Long storageUsed = imageService.getTotalStorageUsedByUserId(currentUser.getId());
            Integer imageCount = imageService.countImagesByUserId(currentUser.getId());
            model.addAttribute("storageUsed", storageUsed);
            model.addAttribute("imageCount", imageCount);
            
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        model.addAttribute("pageTitle", "Claudinary - Image Hosting");
        return "index";
    }

    @GetMapping("/index")
    public String indexRedirect() {
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("SPRING_SECURITY_CONTEXT") != null) {
            return "redirect:/";
        }
        
        model.addAttribute("loginRequest", new UserLoginRequest());
        model.addAttribute("pageTitle", "Login - Claudinary");
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@Valid @ModelAttribute("loginRequest") UserLoginRequest loginRequest,
                          BindingResult bindingResult,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        
        try {
            authService.login(loginRequest);
            return "redirect:/";
        } catch (Exception e) {
            bindingResult.rejectValue("username", "error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new UserRegisterRequest());
        model.addAttribute("pageTitle", "Register - Claudinary");
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(@Valid @ModelAttribute("registerRequest") UserRegisterRequest registerRequest,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            bindingResult.rejectValue("username", "error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return "redirect:/login";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            
            List<ProjectResponse> projects = projectService.getCurrentUserProjects();
            model.addAttribute("projects", projects);
            
            model.addAttribute("pageTitle", "Upload Image - Claudinary");
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "upload";
    }

    @GetMapping("/gallery")
    public String gallery(Model model, 
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size,
                         @RequestParam(required = false) String search,
                         @RequestParam(required = false) UUID projectId) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ImageResponse> images;
            
            if (projectId != null) {
                images = imageService.getImagesByProjectId(projectId, pageable);
            } else if (search != null && !search.isBlank()) {
                // For search, we'll use the search method
                List<ImageResponse> searchResults = imageService.searchImagesByUserId(currentUser.getId(), search);
                model.addAttribute("images", searchResults);
                model.addAttribute("totalElements", searchResults.size());
                model.addAttribute("totalPages", 1);
                model.addAttribute("currentPage", 0);
            } else {
                images = imageService.getImagesByUserId(currentUser.getId(), pageable);
                model.addAttribute("images", images.getContent());
                model.addAttribute("totalElements", images.getTotalElements());
                model.addAttribute("totalPages", images.getTotalPages());
                model.addAttribute("currentPage", images.getNumber());
            }
            
            List<ProjectResponse> projects = projectService.getCurrentUserProjects();
            model.addAttribute("projects", projects);
            
            if (projectId != null) {
                ProjectResponse currentProject = projectService.getProjectById(projectId);
                model.addAttribute("currentProject", currentProject);
            }
            
            model.addAttribute("searchQuery", search);
            model.addAttribute("selectedProjectId", projectId);
            model.addAttribute("pageTitle", "My Gallery - Claudinary");
            
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "gallery";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            
            List<ProjectResponse> projects = projectService.getCurrentUserProjects();
            model.addAttribute("projects", projects);
            
            ProjectResponse defaultProject = projectService.getDefaultProjectForCurrentUserAsResponse();
            model.addAttribute("defaultProject", defaultProject);
            
            model.addAttribute("newProject", new ProjectRequest());
            model.addAttribute("pageTitle", "My Projects - Claudinary");
            
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "projects";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            
            // Get storage info
            Long storageUsed = imageService.getTotalStorageUsedByUserId(currentUser.getId());
            Integer imageCount = imageService.countImagesByUserId(currentUser.getId());
            Integer projectCount = projectService.countProjectsByUserId(currentUser.getId());
            
            model.addAttribute("storageUsed", storageUsed);
            model.addAttribute("imageCount", imageCount);
            model.addAttribute("projectCount", projectCount);
            
            model.addAttribute("pageTitle", "My Profile - Claudinary");
            
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "profile";
    }

    @GetMapping("/image/{imageId}")
    public String viewImage(@PathVariable UUID imageId, Model model) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            
            ImageResponse image = imageService.getImageById(imageId);
            model.addAttribute("image", image);
            
            // Increment view count
            imageService.incrementViewCount(imageId);
            
            model.addAttribute("pageTitle", image.getOriginalFilename() + " - Claudinary");
            
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "view-image";
    }

    @GetMapping("/about")
    public String about(Model model) {
        try {
            UserResponse currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
        } catch (Exception e) {
            // Not authenticated, that's fine
        }
        model.addAttribute("pageTitle", "About - Claudinary");
        return "about";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied - Claudinary");
        return "access-denied";
    }
}
