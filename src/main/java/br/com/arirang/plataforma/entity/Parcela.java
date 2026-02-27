package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "parcelas")
public class Parcela {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NotNull(message = "Contrato é obrigatório")
    private Contrato contrato;
    
    @NotNull(message = "Número da parcela é obrigatório")
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;
    
    @NotNull(message = "Valor da parcela é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor da parcela deve ser maior que zero")
    @Column(name = "valor_parcela", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorParcela;
    
    @NotNull(message = "Data de vencimento é obrigatória")
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;
    
    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;
    
    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago;
    
    @Column(name = "juros_aplicados", precision = 10, scale = 2)
    private BigDecimal jurosAplicados;
    
    @Column(name = "multa_aplicada", precision = 10, scale = 2)
    private BigDecimal multaAplicada;
    
    @Column(name = "desconto_aplicado", precision = 10, scale = 2)
    private BigDecimal descontoAplicado;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_parcela", nullable = false)
    private StatusParcela statusParcela;
    
    @Column(name = "observacoes", length = 255)
    private String observacoes;
    
    // Construtor padrão
    public Parcela() {
        this.statusParcela = StatusParcela.PENDENTE;
    }
    
    // Construtor com campos obrigatórios
    public Parcela(Contrato contrato, Integer numeroParcela, BigDecimal valorParcela, LocalDate dataVencimento) {
        this();
        this.contrato = contrato;
        this.numeroParcela = numeroParcela;
        this.valorParcela = valorParcela;
        this.dataVencimento = dataVencimento;
    }
    
    // Métodos de negócio
    public boolean isPaga() {
        return StatusParcela.PAGA.equals(this.statusParcela);
    }
    
    public boolean isVencida() {
        return LocalDate.now().isAfter(this.dataVencimento) && !isPaga();
    }
    
    public boolean isEmAtraso() {
        return isVencida() && StatusParcela.EM_ATRASO.equals(this.statusParcela);
    }
    
    public BigDecimal getValorComJurosEMulta() {
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
    
    public void marcarComoPaga(BigDecimal valorPago) {
        this.dataPagamento = LocalDate.now();
        this.valorPago = valorPago;
        this.statusParcela = StatusParcela.PAGA;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
    
    public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(Integer numeroParcela) { this.numeroParcela = numeroParcela; }
    
    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }
    
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    
    public BigDecimal getJurosAplicados() { return jurosAplicados; }
    public void setJurosAplicados(BigDecimal jurosAplicados) { this.jurosAplicados = jurosAplicados; }
    
    public BigDecimal getMultaAplicada() { return multaAplicada; }
    public void setMultaAplicada(BigDecimal multaAplicada) { this.multaAplicada = multaAplicada; }
    
    public BigDecimal getDescontoAplicado() { return descontoAplicado; }
    public void setDescontoAplicado(BigDecimal descontoAplicado) { this.descontoAplicado = descontoAplicado; }
    
    public StatusParcela getStatusParcela() { return statusParcela; }
    public void setStatusParcela(StatusParcela statusParcela) { this.statusParcela = statusParcela; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
