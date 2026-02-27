package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.Endereco;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeclaracaoMatriculaDTO(
        Long alunoId,
        
        String alunoNome,
        
        String alunoCpf,
        
        String alunoRg,
        
        LocalDate alunoDataNascimento,
        
        Endereco alunoEndereco,
        
        Long turmaId,
        
        String turmaNome,
        
        String nivelProficiencia,
        
        String professorNome,
        
        String diaTurma,
        
        String turno,
        
        String horaInicio,
        
        String horaTermino,
        
        LocalDate inicioTurma,
        
        LocalDate terminoTurma,
        
        LocalDate dataContrato,
        
        BigDecimal valorMatricula,
        
        BigDecimal valorMensalidade,
        
        Integer numeroParcelas,
        
        LocalDate dataGeracao,
        
        String numeroDeclaracao
) {
    
    public DeclaracaoMatriculaDTO {
        if (dataGeracao == null) {
            dataGeracao = LocalDate.now();
        }
    }
}
