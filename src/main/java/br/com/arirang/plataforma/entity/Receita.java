package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receitas")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NotNull(message = "Contrato é obrigatório")
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @NotNull(message = "Aluno é obrigatório")
    private Aluno aluno;

    @Column(name = "tipo_receita", nullable = false, length = 20)
    @NotNull(message = "Tipo da receita é obrigatório")
    private String tipoReceita; // MATRICULA, MENSALIDADE, MATERIAL, OUTROS

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "valor_original", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor original é obrigatório")
    @DecimalMin(value = "0.0", message = "Valor deve ser positivo")
    private BigDecimal valorOriginal;

    @Column(name = "valor_desconto", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Desconto deve ser positivo")
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_juros", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Juros devem ser positivos")
    private BigDecimal valorJuros = BigDecimal.ZERO;

    @Column(name = "valor_final", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Valor final é obrigatório")
    @DecimalMin(value = "0.0", message = "Valor final deve ser positivo")
    private BigDecimal valorFinal;

    @Column(name = "data_vencimento", nullable = false)
    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "situacao", length = 20, nullable = false)
    private String situacao = "PENDENTE"; // PENDENTE, PAGO, VENCIDO, CANCELADO

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_criacao", length = 100)
    private String usuarioCriacao;

    // Construtor padrão
    public Receita() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de negócio
    public boolean isVencida() {
        return LocalDate.now().isAfter(dataVencimento) && "PENDENTE".equals(situacao);
    }

    public boolean isPaga() {
        return "PAGO".equals(situacao);
    }

    public long getDiasAtraso() {
        if (isPaga() || !isVencida()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dataVencimento, LocalDate.now());
    }

    public BigDecimal calcularJuros(BigDecimal taxaJurosMensal) {
        if (!isVencida() || taxaJurosMensal == null) {
            return BigDecimal.ZERO;
        }
        
        long diasAtraso = getDiasAtraso();
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Cálculo de juros simples: (valor * taxa * dias) / 30
        BigDecimal taxaDiaria = taxaJurosMensal.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        return valorOriginal.multiply(taxaDiaria).multiply(BigDecimal.valueOf(diasAtraso));
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public String getTipoReceita() { return tipoReceita; }
    public void setTipoReceita(String tipoReceita) { this.tipoReceita = tipoReceita; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValorOriginal() { return valorOriginal; }
    public void setValorOriginal(BigDecimal valorOriginal) { this.valorOriginal = valorOriginal; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public BigDecimal getValorJuros() { return valorJuros; }
    public void setValorJuros(BigDecimal valorJuros) { this.valorJuros = valorJuros; }

    public BigDecimal getValorFinal() { return valorFinal; }
    public void setValorFinal(BigDecimal valorFinal) { this.valorFinal = valorFinal; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(Integer numeroParcela) { this.numeroParcela = numeroParcela; }

    public Integer getTotalParcelas() { return totalParcelas; }
    public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getUsuarioCriacao() { return usuarioCriacao; }
    public void setUsuarioCriacao(String usuarioCriacao) { this.usuarioCriacao = usuarioCriacao; }
}
