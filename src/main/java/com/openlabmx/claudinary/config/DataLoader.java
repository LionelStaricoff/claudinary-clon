package com.openlabmx.claudinary.config;

import com.openlabmx.claudinary.entity.Role;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.enums.RoleType;
import com.openlabmx.claudinary.repository.RoleRepository;
import com.openlabmx.claudinary.repository.UserRepository;
import com.openlabmx.claudinary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectService projectService;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // Only run in development profile
        if (!Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            return;
        }

        // Create roles
        Set<Role> roles = createRoles();
        
        // Create admin user
        User adminUser = createAdminUser(roles);
        
        // Create test user
        User testUser = createTestUser(roles);
        
        log.info("Data initialization completed");
    }

    private Set<Role> createRoles() {
        Set<Role> roles = new HashSet<>();
        
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
            .orElseGet(() -> {
                Role role = Role.builder()
                    .name(RoleType.ROLE_ADMIN)
                    .description("Administrator role with full access")
                    .build();
                return roleRepository.save(role);
            });
        roles.add(adminRole);

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
            .orElseGet(() -> {
                Role role = Role.builder()
                    .name(RoleType.ROLE_USER)
                    .description("Regular user role")
                    .build();
                return roleRepository.save(role);
            });
        roles.add(userRole);

        return roles;
    }

    private User createAdminUser(Set<Role> roles) {
        if (userRepository.existsByUsername("admin")) {
            return userRepository.findByUsername("admin").get();
        }

        Role adminRole = roles.stream()
            .filter(role -> role.getName() == RoleType.ROLE_ADMIN)
            .findFirst()
            .orElse(null);

        Role userRole = roles.stream()
            .filter(role -> role.getName() == RoleType.ROLE_USER)
            .findFirst()
            .orElse(null);

        User admin = User.builder()
            .username("admin")
            .email("admin@claudinary.com")
            .password(passwordEncoder.encode("admin123"))
            .firstName("Admin")
            .lastName("User")
            .storageUsed(0L)
            .storageLimit(100L * 1024L * 1024L * 10) // 1GB for admin
            .isActive(true)
            .isLocked(false)
            .failedLoginAttempts(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        if (adminRole != null) {
            admin.addRole(adminRole);
        }
        if (userRole != null) {
            admin.addRole(userRole);
        }

        admin = userRepository.save(admin);
        
        // Create default project for admin
        projectService.createDefaultProject(admin);
        
        log.info("Created admin user with username: {}", admin.getUsername());
        return admin;
    }

    private User createTestUser(Set<Role> roles) {
        if (userRepository.existsByUsername("testuser")) {
            return userRepository.findByUsername("testuser").get();
        }

        Role userRole = roles.stream()
            .filter(role -> role.getName() == RoleType.ROLE_USER)
            .findFirst()
            .orElse(null);

        User testUser = User.builder()
            .username("testuser")
            .email("testuser@claudinary.com")
            .password(passwordEncoder.encode("test123"))
            .firstName("Test")
            .lastName("User")
            .storageUsed(0L)
            .storageLimit(100L * 1024L * 1024L) // 100MB
            .isActive(true)
            .isLocked(false)
            .failedLoginAttempts(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        if (userRole != null) {
            testUser.addRole(userRole);
        }

        testUser = userRepository.save(testUser);
        
        // Create default project for test user
        projectService.createDefaultProject(testUser);
        
        log.info("Created test user with username: {}", testUser.getUsername());
        return testUser;
    }
}
