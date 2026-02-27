package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.Usuario;

public record LoginResponse(
    String token,
    String username,
    String nomeCompleto,
    String role,
    Long id
) {
    public static LoginResponse from(Usuario usuario, String token) {
        return new LoginResponse(
            token,
            usuario.getUsername(),
            usuario.getNomeCompleto(),
            usuario.getRole().name(),
            usuario.getId()
        );
    }
}
