package com.openlabmx.claudinary.dto.response;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private UUID userId;
    private String username;
    private String email;
    private List<String> roles;
    private String message;
    private Boolean success;
}
