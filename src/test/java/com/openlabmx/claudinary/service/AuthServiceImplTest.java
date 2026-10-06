package com.openlabmx.claudinary.service;

import com.openlabmx.claudinary.dto.request.UserLoginRequest;
import com.openlabmx.claudinary.dto.request.UserRegisterRequest;
import com.openlabmx.claudinary.dto.response.JwtResponse;
import com.openlabmx.claudinary.entity.Role;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.enums.RoleType;
import com.openlabmx.claudinary.exception.BadRequestException;
import com.openlabmx.claudinary.exception.UnauthorizedException;
import com.openlabmx.claudinary.repository.RoleRepository;
import com.openlabmx.claudinary.repository.UserRepository;
import com.openlabmx.claudinary.security.JwtTokenProvider;
import com.openlabmx.claudinary.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private AuthServiceImpl authService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userRole = Role.builder().id(UUID.randomUUID()).name(RoleType.ROLE_USER).description("user").build();
    }

    @Test
    void register_asignaRoleUserYGeneraJwt() {
        UserRegisterRequest request = UserRegisterRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .password("Password1")
            .firstName("New")
            .lastName("User")
            .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(86400L);

        JwtResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getRoles())
            .as("El usuario registrado debe tener ROLE_USER asignado")
            .singleElement()
            .isEqualTo(userRole);
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getIsLocked()).isFalse();

        verify(userService, times(1)).createDefaultProject(any(User.class));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void register_usernameDuplicado_noPersisteUsuario() {
        UserRegisterRequest request = UserRegisterRequest.builder()
            .username("dup")
            .email("dup@example.com")
            .password("Password1")
            .build();
        when(userRepository.existsByUsername("dup")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void login_passwordValido_generaJwtYAuthenticationEnContext() {
        UUID userId = UUID.randomUUID();
        User stored = new User();
        stored.setId(userId);
        stored.setUsername("admin");
        stored.setEmail("admin@example.com");
        stored.setPassword("encoded-pw");
        stored.setIsActive(true);
        stored.setIsLocked(false);
        stored.setFailedLoginAttempts(0);
        stored.addRole(userRole);

        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("secret123", "encoded-pw")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(stored);
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access-jwt");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-jwt");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(86400L);

        UserLoginRequest request = UserLoginRequest.builder()
            .usernameOrEmail("admin")
            .password("secret123")
            .build();

        JwtResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        assertThat(response.getAccessToken()).isEqualTo("access-jwt");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .as("Login debe popular el SecurityContext sin pasar por AuthenticationManager")
            .isNotNull()
            .matches(a -> a.isAuthenticated());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
            .extracting("authority")
            .contains("ROLE_USER");
    }

    @Test
    void login_passwordInvalido_lanzaUnauthorizedYNoEmiteToken() {
        User stored = new User();
        stored.setId(UUID.randomUUID());
        stored.setUsername("admin");
        stored.setPassword("encoded");
        stored.setIsActive(true);
        stored.setIsLocked(false);
        stored.setFailedLoginAttempts(0);

        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(stored);

        UserLoginRequest request = UserLoginRequest.builder()
            .usernameOrEmail("admin")
            .password("wrong")
            .build();

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(UnauthorizedException.class);

        verify(jwtTokenProvider, never()).generateAccessToken(any());
        assertThat(stored.getFailedLoginAttempts()).isEqualTo(1);
    }
}
