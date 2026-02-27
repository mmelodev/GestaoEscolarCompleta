package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.LoginRequest;
import br.com.arirang.plataforma.dto.LoginResponse;
import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.repository.UsuarioRepository;
import br.com.arirang.plataforma.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setEmail("admin@arirang.com");
        usuario.setPassword("encodedPassword");
        usuario.setNomeCompleto("Administrador");
        usuario.setRole(Usuario.Role.ADMIN);
        usuario.setAtivo(true);

        loginRequest = new LoginRequest("admin", "admin123");
    }

    @Test
    void testAuthenticateSuccess() {
        // Given
        when(usuarioRepository.findActiveByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(usuario)).thenReturn("token123");

        // When
        LoginResponse response = usuarioService.authenticate(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("token123", response.token());
        assertEquals("admin", response.username());
        assertEquals("Administrador", response.nomeCompleto());
        assertEquals("ADMIN", response.role());
        assertEquals(1L, response.id());

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void testAuthenticateUserNotFound() {
        // Given
        when(usuarioRepository.findActiveByUsername("admin")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            usuarioService.authenticate(loginRequest);
        });
    }

    @Test
    void testAuthenticateWrongPassword() {
        // Given
        when(usuarioRepository.findActiveByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            usuarioService.authenticate(loginRequest);
        });
    }

    @Test
    void testCriarUsuarioSuccess() {
        // Given
        when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);
        when(usuarioRepository.existsByEmail("newuser@arirang.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        Usuario result = usuarioService.criarUsuario("newuser", "newuser@arirang.com", 
                "password123", "Novo Usuário", Usuario.Role.USER);

        // Then
        assertNotNull(result);
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
