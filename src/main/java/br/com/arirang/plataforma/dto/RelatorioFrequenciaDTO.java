package br.com.arirang.plataforma.dto;

import java.time.LocalDate;

/**
 * DTO para relatórios de frequência
 */
public record RelatorioFrequenciaDTO(
    Long alunoId,
    String alunoNome,
    Long turmaId,
    String turmaNome,
    Long totalAulas,
    Long totalPresencas,
    Long totalFaltas,
    Long totalFaltasJustificadas,
    Long totalAtrasos,
    Double percentualFrequencia,
    Boolean abaixoDoMinimo,
    LocalDate dataInicio,
    LocalDate dataFim
) {
    public static RelatorioFrequenciaDTO calcular(
            Long alunoId,
            String alunoNome,
            Long turmaId,
            String turmaNome,
            Long totalAulas,
            Long totalPresencas,
            Long totalFaltas,
            Long totalFaltasJustificadas,
            Long totalAtrasos,
            LocalDate dataInicio,
            LocalDate dataFim,
            Double percentualMinimo) {
        
        Double percentualFrequencia = totalAulas > 0 
            ? (totalPresencas * 100.0 / totalAulas) 
            : 0.0;
        
        Boolean abaixoDoMinimo = percentualFrequencia < percentualMinimo;
        
        return new RelatorioFrequenciaDTO(
            alunoId, alunoNome, turmaId, turmaNome,
            totalAulas, totalPresencas, totalFaltas,
            totalFaltasJustificadas, totalAtrasos,
            percentualFrequencia, abaixoDoMinimo,
            dataInicio, dataFim
        );
    }
}
