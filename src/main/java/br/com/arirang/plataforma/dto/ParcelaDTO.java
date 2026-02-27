package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.StatusParcela;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaDTO(
        Long id,
        
        @NotNull(message = "ID do contrato é obrigatório")
        Long contratoId,
        
        @NotNull(message = "Número da parcela é obrigatório")
        Integer numeroParcela,
        
        @NotNull(message = "Valor da parcela é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor da parcela deve ser maior que zero")
        BigDecimal valorParcela,
        
        @NotNull(message = "Data de vencimento é obrigatória")
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
    
    public ParcelaDTO {
        if (valorComJurosEMulta == null && valorParcela != null) {
            valorComJurosEMulta = calcularValorComJurosEMulta();
        }
        
        if (vencida == false && dataVencimento != null) {
            vencida = LocalDate.now().isAfter(dataVencimento) && !StatusParcela.PAGA.equals(statusParcela);
        }
        
        if (emAtraso == false && vencida) {
            emAtraso = StatusParcela.EM_ATRASO.equals(statusParcela);
        }
    }
    
    private BigDecimal calcularValorComJurosEMulta() {
        BigDecimal valorTotal = valorParcela;
        
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
