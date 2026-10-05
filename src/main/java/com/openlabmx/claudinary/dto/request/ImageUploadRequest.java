package com.openlabmx.claudinary.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImageUploadRequest {
    @NotNull(message = "Image file is required")
    private MultipartFile file;
    @Size(max = 255)
    private String originalFilename;
    @Size(max = 100)
    private String title;
    @Size(max = 500)
    private String description;
    @Builder.Default
    private Boolean isPublic = true;
    private UUID projectId;
    private String tags;
    public void validateFile() {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Image file is required");
        long maxSize = 10L * 1024L * 1024L;
        if (file.getSize() > maxSize) throw new IllegalArgumentException("File size exceeds 10MB limit");
    }
}
