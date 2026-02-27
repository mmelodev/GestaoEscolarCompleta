package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.Endereco;
import br.com.arirang.plataforma.entity.StatusContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FichaMatriculaDTO(
        Long alunoId,
        
        String alunoNome,
        
        String alunoNomeSocial,
        
        String alunoCpf,
        
        String alunoRg,
        
        String alunoOrgaoExpeditorRg,
        
        String alunoNacionalidade,
        
        String alunoUf,
        
        LocalDate alunoDataNascimento,
        
        String alunoGenero,
        
        String alunoEmail,
        
        String alunoTelefone,
        
        Endereco alunoEndereco,
        
        String alunoSituacao,
        
        String alunoUltimoNivel,
        
        String responsavelNome,
        
        String responsavelCpf,
        
        String responsavelTelefone,
        
        String responsavelEmail,
        
        String grauParentesco,
        
        boolean responsavelFinanceiro,
        
        List<ContratoResumoDTO> contratos,
        
        LocalDate dataGeracao,
        
        String numeroFicha
) {
    
    public FichaMatriculaDTO {
        if (dataGeracao == null) {
            dataGeracao = LocalDate.now();
        }
    }
    
    public record ContratoResumoDTO(
            Long id,
            String turmaNome,
            String nivelProficiencia,
            LocalDate dataContrato,
            BigDecimal valorMatricula,
            BigDecimal valorMensalidade,
            Integer numeroParcelas,
            StatusContrato statusContrato
    ) {}
}
