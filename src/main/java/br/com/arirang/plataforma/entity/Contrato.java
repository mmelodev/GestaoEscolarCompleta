package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @NotNull(message = "Aluno é obrigatório")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @NotNull(message = "Turma é obrigatória")
    private Turma turma;

    @Column(name = "numero_contrato", unique = true, nullable = false)
    private String numeroContrato;

    @Column(name = "data_contrato", nullable = false)
    @NotNull(message = "Data do contrato é obrigatória")
    private LocalDate dataContrato;

    @Column(name = "data_inicio_vigencia")
    private LocalDate dataInicioVigencia;

    @Column(name = "data_fim_vigencia")
    private LocalDate dataFimVigencia;

    // Parte Financeira
    @Column(name = "valor_matricula", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Valor da matrícula deve ser positivo")
    private BigDecimal valorMatricula;

    @Column(name = "valor_mensalidade", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Valor da mensalidade deve ser positivo")
    private BigDecimal valorMensalidade;

    @Column(name = "numero_parcelas")
    private Integer numeroParcelas;

    @Column(name = "desconto_valor", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Desconto deve ser positivo")
    private BigDecimal descontoValor;

    @Column(name = "desconto_percentual", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Desconto percentual deve ser positivo")
    private BigDecimal descontoPercentual;

    @Column(name = "valor_total_contrato", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Valor total deve ser positivo")
    private BigDecimal valorTotalContrato;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "situacao_contrato", length = 20)
    private String situacaoContrato = "ATIVO"; // ATIVO, CANCELADO, SUSPENSO

    @Column(name = "template_pdf", length = 50)
    private String templatePdf; // contrato-servicos-menor, contrato-curso, uso-imagem-menor, uso-imagem-adulto

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Construtor padrão
    public Contrato() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }

    public String getNumeroContrato() { return numeroContrato; }
    public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; }

    public LocalDate getDataContrato() { return dataContrato; }
    public void setDataContrato(LocalDate dataContrato) { this.dataContrato = dataContrato; }

    public LocalDate getDataInicioVigencia() { return dataInicioVigencia; }
    public void setDataInicioVigencia(LocalDate dataInicioVigencia) { this.dataInicioVigencia = dataInicioVigencia; }

    public LocalDate getDataFimVigencia() { return dataFimVigencia; }
    public void setDataFimVigencia(LocalDate dataFimVigencia) { this.dataFimVigencia = dataFimVigencia; }

    public BigDecimal getValorMatricula() { return valorMatricula; }
    public void setValorMatricula(BigDecimal valorMatricula) { this.valorMatricula = valorMatricula; }

    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valorMensalidade) { this.valorMensalidade = valorMensalidade; }

    public Integer getNumeroParcelas() { return numeroParcelas; }
    public void setNumeroParcelas(Integer numeroParcelas) { this.numeroParcelas = numeroParcelas; }

    public BigDecimal getDescontoValor() { return descontoValor; }
    public void setDescontoValor(BigDecimal descontoValor) { this.descontoValor = descontoValor; }

    public BigDecimal getDescontoPercentual() { return descontoPercentual; }
    public void setDescontoPercentual(BigDecimal descontoPercentual) { this.descontoPercentual = descontoPercentual; }

    public BigDecimal getValorTotalContrato() { return valorTotalContrato; }
    public void setValorTotalContrato(BigDecimal valorTotalContrato) { this.valorTotalContrato = valorTotalContrato; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getSituacaoContrato() { return situacaoContrato; }
    public void setSituacaoContrato(String situacaoContrato) { this.situacaoContrato = situacaoContrato; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getTemplatePdf() { return templatePdf; }
    public void setTemplatePdf(String templatePdf) { this.templatePdf = templatePdf; }
}
