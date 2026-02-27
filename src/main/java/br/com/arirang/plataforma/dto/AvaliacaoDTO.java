package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.TipoNota;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AvaliacaoDTO(
        Long id,
        @NotBlank(message = "Nome da avaliação é obrigatório")
        String nomeAvaliacao,
        String descricao,
        @NotNull(message = "Tipo de avaliação é obrigatório")
        TipoNota tipoAvaliacao,
        @NotNull(message = "Turma é obrigatória")
        Long turmaId,
        String nomeTurma, // Para exibição
        @NotNull(message = "Data da avaliação é obrigatória")
        @FutureOrPresent(message = "A data da avaliação não pode ser no passado")
        LocalDate dataAvaliacao,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        @NotNull(message = "Peso da avaliação é obrigatório")
        @Min(value = 1, message = "Peso deve ser no mínimo 1")
        @Max(value = 10, message = "Peso deve ser no máximo 10")
        Integer peso,
        @NotNull(message = "Valor máximo da avaliação é obrigatório")
        @Min(value = 1, message = "Valor máximo deve ser no mínimo 1")
        @Max(value = 100, message = "Valor máximo deve ser no máximo 100")
        Integer valorMaximo,
        Boolean ativa,
        List<NotaAvaliacaoDTO> notas
) {
}