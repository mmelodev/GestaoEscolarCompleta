package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.enums.TipoPresenca;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de frequência
 */
public record FrequenciaDTO(
    Long id,
    Long alunoId,
    String alunoNome,
    Long turmaId,
    String turmaNome,
    LocalDate dataAula,
    TipoPresenca tipoPresenca,
    String observacao,
    String justificativa,
    LocalDateTime dataRegistro,
    String registradoPorNome
) {
}
