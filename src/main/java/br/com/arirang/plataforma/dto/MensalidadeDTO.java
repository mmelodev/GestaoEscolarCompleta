package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.StatusParcela;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MensalidadeDTO(
        Long id,
        Long contratoId,
        String numeroContrato,
        Long alunoId,
        String alunoNome,
        String alunoEmail,
        String alunoTelefone,
        Long turmaId,
        String turmaNome,
        Integer numeroParcela,
        BigDecimal valorParcela,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        BigDecimal valorPago,
        BigDecimal jurosAplicados,
        BigDecimal multaAplicada,
        BigDecimal descontoAplicado,
        StatusParcela statusParcela,
        String observacoes,
        BigDecimal valorComJurosEMulta,
        boolean vencida,
        boolean emAtraso
) {
    public MensalidadeDTO {
        if (valorComJurosEMulta == null && valorParcela != null) {
            valorComJurosEMulta = calcularValorComJurosEMulta();
        }
        
        if (!vencida && dataVencimento != null) {
            vencida = LocalDate.now().isAfter(dataVencimento) && !StatusParcela.PAGA.equals(statusParcela);
        }
        
        if (!emAtraso && vencida) {
            emAtraso = StatusParcela.EM_ATRASO.equals(statusParcela);
        }
    }
    
    private BigDecimal calcularValorComJurosEMulta() {
        BigDecimal valorTotal = valorParcela != null ? valorParcela : BigDecimal.ZERO;
        
        if (jurosAplicados != null) {
            valorTotal = valorTotal.add(jurosAplicados);
        }
        
        if (multaAplicada != null) {
            valorTotal = valorTotal.add(multaAplicada);
        }
        
        if (descontoAplicado != null) {
            valorTotal = valorTotal.subtract(descontoAplicado);
        }
        
        return valorTotal.max(BigDecimal.ZERO);
    }
}

