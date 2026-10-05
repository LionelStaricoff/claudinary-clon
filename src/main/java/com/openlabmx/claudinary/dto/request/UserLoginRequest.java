package com.openlabmx.claudinary.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserLoginRequest {
    @NotBlank @Size(max = 100)
    private String usernameOrEmail;
    @NotBlank @Size(min = 8, max = 100)
    private String password;
    @Builder.Default
    private Boolean rememberMe = false;
}
