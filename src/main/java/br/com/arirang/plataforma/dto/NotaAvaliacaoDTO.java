package br.com.arirang.plataforma.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record NotaAvaliacaoDTO(
        Long id,
        @NotNull(message = "ID da avaliação é obrigatório")
        Long avaliacaoId,
        @NotNull(message = "ID do aluno é obrigatório")
        Long alunoId,
        String nomeAluno, // Para exibição
        @Min(value = 0, message = "Nota deve ser no mínimo 0")
        @Max(value = 100, message = "Nota deve ser no máximo 100")
        Integer valorNota,
        String observacoes,
        LocalDateTime dataLancamento,
        LocalDateTime dataAtualizacao,
        @NotNull(message = "Presença é obrigatória")
        Boolean presente
) {
}