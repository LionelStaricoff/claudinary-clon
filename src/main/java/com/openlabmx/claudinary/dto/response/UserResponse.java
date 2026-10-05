package com.openlabmx.claudinary.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private Boolean isLocked;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long storageUsed;
    private Long storageLimit;
    private Integer totalImages;
    private Integer totalProjects;
    private String profileImageUrl;
    private String language;
    private String timezone;
}
