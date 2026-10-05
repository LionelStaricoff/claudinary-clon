package com.openlabmx.claudinary.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImageResponse {
    private UUID id;
    private String filename;
    private String originalFilename;
    private String url;
    private String webpUrl;
    private Long fileSize;
    private Long webpSize;
    private Integer width;
    private Integer height;
    private String contentType;
    private String originalFormat;
    private Boolean isPublic;
    private Integer viewCount;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID projectId;
    private String projectName;
    private Boolean projectIsDefault;
    private UUID userId;
    private String username;
    private String fullUrl;
    private String fullWebpUrl;
    private String thumbnailUrl;
    @Builder.Default
    private Boolean hasWebpVersion = false;
    @Builder.Default
    private Boolean isOptimized = true;
}
