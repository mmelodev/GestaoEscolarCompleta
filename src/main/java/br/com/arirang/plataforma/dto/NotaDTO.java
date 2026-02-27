package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.TipoNota;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotaDTO(
        Long id,
        @NotNull(message = "Tipo da nota é obrigatório")
        TipoNota tipoNota,
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,
        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota deve ser no mínimo 1")
        @Max(value = 100, message = "Nota deve ser no máximo 100")
        Integer valorNota
) {}
