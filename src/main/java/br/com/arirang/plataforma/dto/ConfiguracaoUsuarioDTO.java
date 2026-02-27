package br.com.arirang.plataforma.dto;

/**
 * DTO para configurações de personalização do usuário
 */
public record ConfiguracaoUsuarioDTO(
    Long id,
    Long usuarioId,
    String corPrimaria,
    String corSecundaria,
    String corHeader,
    String corTexto,
    String corDestaque,
    String logoUrl,
    String logoAlt,
    Integer fonteTamanhoBase,
    Boolean bordaArredondada,
    Boolean temaEscuro
) {
    public static ConfiguracaoUsuarioDTO createDefault(Long usuarioId) {
        return new ConfiguracaoUsuarioDTO(
            null,
            usuarioId,
            "#01004e", // Cor primária padrão
            "#860213", // Cor secundária padrão
            "#1a1a1a", // Cor do header padrão
            "#f5f5f5", // Cor do texto padrão
            "#007bff", // Cor de destaque padrão
            null, // Logo padrão (usa o do sistema)
            "Logo AriranG", // Alt text padrão
            16, // Tamanho base da fonte
            true, // Bordas arredondadas
            true // Tema escuro
        );
    }
}
