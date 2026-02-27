package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receita_id", nullable = false)
    @NotNull(message = "Receita é obrigatória")
    private Receita receita;

    @Column(name = "valor_pago", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor pago não pode ser negativo")
    private BigDecimal valorPago;

    @Column(name = "data_pagamento", nullable = false)
    @NotNull(message = "Data do pagamento é obrigatória")
    private LocalDate dataPagamento;

    @Column(name = "forma_pagamento", length = 30, nullable = false)
    @NotNull(message = "Forma de pagamento é obrigatória")
    private String formaPagamento; // DINHEIRO, CARTAO_DEBITO, CARTAO_CREDITO, PIX, BOLETO, TRANSFERENCIA

    @Column(name = "numero_transacao", length = 100)
    private String numeroTransacao;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "comprovante_caminho", length = 255)
    private String comprovanteCaminho;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_pagamento", length = 100)
    private String usuarioPagamento;

    @Column(name = "desconto_percentual", precision = 5, scale = 2)
    private BigDecimal descontoPercentual;

    @Column(name = "desconto_valor", precision = 10, scale = 2)
    private BigDecimal descontoValor;

    // Construtor padrão
    public Pagamento() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de negócio
    public boolean isParcial() {
        return receita != null && valorPago.compareTo(receita.getValorFinal()) < 0;
    }

    public boolean isIntegral() {
        return receita != null && valorPago.compareTo(receita.getValorFinal()) >= 0;
    }

    public BigDecimal getValorRestante() {
        if (receita == null) {
            return BigDecimal.ZERO;
        }
        return receita.getValorFinal().subtract(valorPago).max(BigDecimal.ZERO);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Receita getReceita() { return receita; }
    public void setReceita(Receita receita) { this.receita = receita; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getNumeroTransacao() { return numeroTransacao; }
    public void setNumeroTransacao(String numeroTransacao) { this.numeroTransacao = numeroTransacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getComprovanteCaminho() { return comprovanteCaminho; }
    public void setComprovanteCaminho(String comprovanteCaminho) { this.comprovanteCaminho = comprovanteCaminho; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getUsuarioPagamento() { return usuarioPagamento; }
    public void setUsuarioPagamento(String usuarioPagamento) { this.usuarioPagamento = usuarioPagamento; }

    public BigDecimal getDescontoPercentual() { return descontoPercentual; }
    public void setDescontoPercentual(BigDecimal descontoPercentual) { this.descontoPercentual = descontoPercentual; }

    public BigDecimal getDescontoValor() { return descontoValor; }
    public void setDescontoValor(BigDecimal descontoValor) { this.descontoValor = descontoValor; }
}
