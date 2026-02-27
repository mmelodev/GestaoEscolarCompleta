package br.com.arirang.plataforma.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ConfiguracaoFinanceiraDTO(
        Long id,
        @NotBlank(message = "Chave é obrigatória")
        String chave,
        String valor,
        String descricao,
        @NotBlank(message = "Tipo é obrigatório")
        String tipo,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        String usuarioCriacao
) {
    
    public static ConfiguracaoFinanceiraDTO of(Long id, String chave, String valor, String descricao, String tipo,
                                              Boolean ativo, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao, String usuarioCriacao) {
        return new ConfiguracaoFinanceiraDTO(id, chave, valor, descricao, tipo, ativo, dataCriacao, dataAtualizacao, usuarioCriacao);
    }
    
    public static ConfiguracaoFinanceiraDTO createNew(String chave, String valor, String descricao, String tipo) {
        return new ConfiguracaoFinanceiraDTO(null, chave, valor, descricao, tipo, true, null, null, null);
    }
}
