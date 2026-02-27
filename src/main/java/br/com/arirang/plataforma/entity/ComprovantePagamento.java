package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprovantes_pagamento")
public class ComprovantePagamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NotNull(message = "Contrato é obrigatório")
    private Contrato contrato;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id")
    private Parcela parcela;
    
    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor pago deve ser maior que zero")
    @Column(name = "valor_pago", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorPago;
    
    @NotNull(message = "Data do pagamento é obrigatória")
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;
    
    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;
    
    @Column(name = "banco", length = 100)
    private String banco;
    
    @Column(name = "agencia", length = 20)
    private String agencia;
    
    @Column(name = "conta", length = 20)
    private String conta;
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "arquivo_comprovante", length = 255)
    private String arquivoComprovante;
    
    @Column(name = "validado")
    private boolean validado = false;
    
    @Column(name = "data_validacao")
    private LocalDateTime dataValidacao;
    
    @Column(name = "validado_por", length = 100)
    private String validadoPor;
    
    // Construtor padrão
    public ComprovantePagamento() {
        this.dataCriacao = LocalDateTime.now();
        this.dataPagamento = LocalDate.now();
    }
    
    // Construtor com campos obrigatórios
    public ComprovantePagamento(Contrato contrato, BigDecimal valorPago, FormaPagamento formaPagamento) {
        this();
        this.contrato = contrato;
        this.valorPago = valorPago;
        this.formaPagamento = formaPagamento;
    }
    
    // Métodos de negócio
    public void validar(String validadoPor) {
        this.validado = true;
        this.dataValidacao = LocalDateTime.now();
        this.validadoPor = validadoPor;
    }
    
    public boolean isValido() {
        return this.validado;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
    
    public Parcela getParcela() { return parcela; }
    public void setParcela(Parcela parcela) { this.parcela = parcela; }
    
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    
    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
    
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    
    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public String getArquivoComprovante() { return arquivoComprovante; }
    public void setArquivoComprovante(String arquivoComprovante) { this.arquivoComprovante = arquivoComprovante; }
    
    public boolean isValidado() { return validado; }
    public void setValidado(boolean validado) { this.validado = validado; }
    
    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }
    
    public String getValidadoPor() { return validadoPor; }
    public void setValidadoPor(String validadoPor) { this.validadoPor = validadoPor; }
}
