package com.openlabmx.claudinary.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isPublic;
    private Integer imageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userId;
    private String username;
    private String coverImageUrl;
    private UUID coverImageId;
    private Long totalStorageUsed;
    private Integer totalImages;
}
