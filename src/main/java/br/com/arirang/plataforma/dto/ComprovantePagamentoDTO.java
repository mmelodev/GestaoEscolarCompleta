package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ComprovantePagamentoDTO(
        Long id,
        
        @NotNull(message = "ID do contrato é obrigatório")
        Long contratoId,
        
        Long parcelaId,
        
        @NotNull(message = "Valor pago é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor pago deve ser maior que zero")
        BigDecimal valorPago,
        
        @NotNull(message = "Data do pagamento é obrigatória")
        LocalDate dataPagamento,
        
        LocalDateTime dataCriacao,
        
        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,
        
        String numeroDocumento,
        
        String banco,
        
        String agencia,
        
        String conta,
        
        String observacoes,
        
        String arquivoComprovante,
        
        boolean validado,
        
        LocalDateTime dataValidacao,
        
        String validadoPor
) {
    
    public ComprovantePagamentoDTO {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        
        if (dataPagamento == null) {
            dataPagamento = LocalDate.now();
        }
    }
}
