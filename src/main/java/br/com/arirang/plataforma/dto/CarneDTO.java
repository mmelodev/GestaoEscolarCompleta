package br.com.arirang.plataforma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CarneDTO(
        Long contratoId,
        
        String alunoNome,
        
        String alunoCpf,
        
        String alunoTelefone,
        
        String alunoEmail,
        
        String turmaNome,
        
        String professorNome,
        
        BigDecimal valorMatricula,
        
        BigDecimal valorMensalidade,
        
        Integer numeroParcelas,
        
        LocalDate dataContrato,
        
        BigDecimal valorTotalContrato,
        
        List<ParcelaDTO> parcelas,
        
        LocalDate dataGeracao,
        
        String numeroCarne
) {
    
    public CarneDTO {
        if (dataGeracao == null) {
            dataGeracao = LocalDate.now();
        }
    }
    
    // Métodos de conveniência
    public BigDecimal getValorTotalParcelas() {
        return parcelas.stream()
                .map(ParcelaDTO::valorParcela)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getValorTotalPago() {
        return parcelas.stream()
                .filter(p -> p.valorPago() != null)
                .map(ParcelaDTO::valorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getValorTotalPendente() {
        return parcelas.stream()
                .filter(p -> !p.statusParcela().toString().equals("PAGA"))
                .map(ParcelaDTO::valorParcela)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public long getParcelasPagas() {
        return parcelas.stream()
                .filter(p -> p.statusParcela().toString().equals("PAGA"))
                .count();
    }
    
    public long getParcelasPendentes() {
        return parcelas.stream()
                .filter(p -> p.statusParcela().toString().equals("PENDENTE"))
                .count();
    }
    
    public long getParcelasVencidas() {
        return parcelas.stream()
                .filter(ParcelaDTO::vencida)
                .count();
    }
    
    public long getParcelasEmAtraso() {
        return parcelas.stream()
                .filter(ParcelaDTO::emAtraso)
                .count();
    }
    
    public boolean temParcelasVencidas() {
        return getParcelasVencidas() > 0;
    }
    
    public boolean temParcelasEmAtraso() {
        return getParcelasEmAtraso() > 0;
    }
    
    public boolean isCompletamentePago() {
        return getParcelasPendentes() == 0;
    }
}
