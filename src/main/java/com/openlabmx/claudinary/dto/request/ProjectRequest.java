package com.openlabmx.claudinary.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectRequest {
    @NotBlank @Size(min = 1, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 _.-]+$")
    private String name;
    @Size(max = 500)
    private String description;
    @Builder.Default
    private Boolean isPublic = false;
}
