package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.enums.TipoPresenca;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para formulário de registro de frequência
 */
public record FrequenciaFormDTO(
    @NotNull(message = "Aluno é obrigatório")
    Long alunoId,
    
    @NotNull(message = "Turma é obrigatória")
    Long turmaId,
    
    @NotNull(message = "Data da aula é obrigatória")
    LocalDate dataAula,
    
    @NotNull(message = "Tipo de presença é obrigatório")
    TipoPresenca tipoPresenca,
    
    String observacao,
    String justificativa
) {
}
