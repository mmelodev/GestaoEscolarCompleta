package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.LoginRequest;
import br.com.arirang.plataforma.dto.LoginResponse;
import br.com.arirang.plataforma.dto.UpdateUsuarioPasswordRequest;
import br.com.arirang.plataforma.dto.UpdateUsuarioProfileRequest;
import br.com.arirang.plataforma.dto.UsuarioProfileDTO;
import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.repository.UsuarioRepository;
import br.com.arirang.plataforma.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Cacheable(value = "usuarios", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findActiveByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @CacheEvict(value = "usuarios", key = "#loginRequest.username()", condition = "#loginRequest != null && #loginRequest.username() != null")
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioRepository.findActiveByUsername(loginRequest.username())
                    .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

            if (!passwordEncoder.matches(loginRequest.password(), usuario.getPassword())) {
                throw new RuntimeException("Credenciais inválidas");
            }

            // Atualizar último acesso
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            String token = jwtUtil.generateToken(usuario);
            logger.info("Usuário {} autenticado com sucesso", usuario.getUsername());

            return LoginResponse.from(usuario, token);

        } catch (Exception e) {
            logger.error("Erro na autenticação: {}", e.getMessage());
            throw new RuntimeException("Erro na autenticação: " + e.getMessage());
        }
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario criarUsuario(String username, String email, String password, String nomeCompleto, Usuario.Role role) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("Username já existe");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNomeCompleto(nomeCompleto);
        usuario.setRole(role);
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public void desativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public void ativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioProfileDTO obterPerfil(String username) {
        Usuario usuario = usuarioRepository.findActiveByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        return UsuarioProfileDTO.fromEntity(usuario);
    }

    @CacheEvict(value = "usuarios", key = "#username")
    public UsuarioProfileDTO atualizarPerfil(String username, UpdateUsuarioProfileRequest request) {
        Usuario usuario = usuarioRepository.findActiveByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (usuarioRepository.existsByEmailAndIdNot(request.email(), usuario.getId())) {
            throw new IllegalArgumentException("Email já está em uso por outro usuário");
        }

        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setEmail(request.email());
        usuario.setTelefone(normalize(request.telefone()));
        usuario.setBio(normalize(request.bio()));
        usuario.setAvatarUrl(normalize(request.avatarUrl()));
        usuario.setPerfilAtualizadoEm(LocalDateTime.now());

        Usuario atualizado = usuarioRepository.saveAndFlush(usuario);
        return UsuarioProfileDTO.fromEntity(atualizado);
    }

    @CacheEvict(value = "usuarios", key = "#username")
    public void alterarSenha(String username, UpdateUsuarioPasswordRequest request) {
        Usuario usuario = usuarioRepository.findActiveByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        if (!request.novaSenha().equals(request.confirmarSenha())) {
            throw new IllegalArgumentException("Nova senha e confirmação não conferem");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getPassword())) {
            throw new IllegalArgumentException("Nova senha deve ser diferente da senha atual");
        }

        usuario.setPassword(passwordEncoder.encode(request.novaSenha()));
        usuario.setSenhaAtualizadaEm(LocalDateTime.now());
        usuarioRepository.saveAndFlush(usuario);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
