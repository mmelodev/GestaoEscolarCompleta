package br.com.arirang.plataforma.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import br.com.arirang.plataforma.enums.Turno;
import br.com.arirang.plataforma.enums.Formato;
import br.com.arirang.plataforma.enums.Modalidade;

public record TurmaDTO(
        Long id,
        @NotBlank(message = "Nome da turma é obrigatório")
        @Size(max = 120)
        String nomeTurma,
        @Size(max = 50)
        String idioma,
        Long professorResponsavelId,
        @Size(max = 60)
        String nivelProficiencia,
        @Size(max = 20)
        String diaTurma,
        Turno turno,
        Formato formato,
        Modalidade modalidade,
        @Size(max = 80)
        String realizador,
        @Size(max = 5)
        String horaInicio,
        @Size(max = 5)
        String horaTermino,
        @Size(max = 10)
        String anoSemestre,
        Integer cargaHorariaTotal,
        Integer quantidadeAulas,
        String calendarioPdf,
        LocalDate inicioTurma,          // Alterado de LocalDateTime para LocalDate
        LocalDate terminoTurma,         // Alterado de LocalDateTime para LocalDate
        @Size(max = 30)
        String situacaoTurma,
        List<Long> alunoIds
) {
    // Método helper para criar DTO simples (apenas id, nome, nivel) - usado em seleções
    public static TurmaDTO simple(Long id, String nomeTurma, String nivelProficiencia) {
        return new TurmaDTO(
            id,                    // id
            nomeTurma,             // nomeTurma
            null,                  // idioma
            null,                  // professorResponsavelId
            nivelProficiencia,     // nivelProficiencia
            null,                  // diaTurma
            null,                  // turno
            null,                  // formato
            null,                  // modalidade
            null,                  // realizador
            null,                  // horaInicio
            null,                  // horaTermino
            null,                  // anoSemestre
            null,                  // cargaHorariaTotal
            null,                  // quantidadeAulas
            null,                  // calendarioPdf
            null,                  // inicioTurma
            null,                  // terminoTurma
            null,                  // situacaoTurma
            null                   // alunoIds
        );
    }
}