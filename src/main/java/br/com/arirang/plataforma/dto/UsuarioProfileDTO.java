package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioProfileDTO(
        Long id,
        String username,
        String email,
        String nomeCompleto,
        String telefone,
        String bio,
        String avatarUrl,
        Usuario.Role role,
        LocalDateTime dataCriacao,
        LocalDateTime ultimoAcesso,
        LocalDateTime senhaAtualizadaEm,
        LocalDateTime perfilAtualizadoEm
) {
    public static UsuarioProfileDTO fromEntity(Usuario usuario) {
        return new UsuarioProfileDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNomeCompleto(),
                usuario.getTelefone(),
                usuario.getBio(),
                usuario.getAvatarUrl(),
                usuario.getRole(),
                usuario.getDataCriacao(),
                usuario.getUltimoAcesso(),
                usuario.getSenhaAtualizadaEm(),
                usuario.getPerfilAtualizadoEm()
        );
    }
}

