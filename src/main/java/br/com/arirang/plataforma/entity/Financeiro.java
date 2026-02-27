package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financeiro")
public class Financeiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Tipo de movimento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentoFinanceiro tipoMovimento;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @NotNull(message = "Data do movimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataMovimento;
    
    @Column(length = 255)
    private String descricao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 50)
    private CategoriaFinanceira categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id")
    private Parcela parcela;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;
    
    @Column(name = "referencia", length = 100)
    private String referencia; // Ex: "Matrícula 2024.1", "Mensalidade Jan/2024"
    
    @Column(name = "confirmado")
    private boolean confirmado = false;
    
    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;
    
    @Column(name = "confirmado_por", length = 100)
    private String confirmadoPor;
    
    // Construtor padrão
    public Financeiro() {
        this.dataCriacao = LocalDateTime.now();
        this.dataMovimento = LocalDate.now();
    }
    
    // Construtor com campos obrigatórios
    public Financeiro(TipoMovimentoFinanceiro tipoMovimento, BigDecimal valor, CategoriaFinanceira categoria) {
        this();
        this.tipoMovimento = tipoMovimento;
        this.valor = valor;
        this.categoria = categoria;
    }
    
    // Métodos de negócio
    public void confirmar(String confirmadoPor) {
        this.confirmado = true;
        this.dataConfirmacao = LocalDateTime.now();
        this.confirmadoPor = confirmadoPor;
    }
    
    public boolean isReceita() {
        return TipoMovimentoFinanceiro.RECEITA.equals(this.tipoMovimento);
    }
    
    public boolean isDespesa() {
        return TipoMovimentoFinanceiro.DESPESA.equals(this.tipoMovimento);
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TipoMovimentoFinanceiro getTipoMovimento() { return tipoMovimento; }
    public void setTipoMovimento(TipoMovimentoFinanceiro tipoMovimento) { this.tipoMovimento = tipoMovimento; }
    
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    
    public LocalDate getDataMovimento() { return dataMovimento; }
    public void setDataMovimento(LocalDate dataMovimento) { this.dataMovimento = dataMovimento; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public CategoriaFinanceira getCategoria() { return categoria; }
    public void setCategoria(CategoriaFinanceira categoria) { this.categoria = categoria; }
    
    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }
    
    public Parcela getParcela() { return parcela; }
    public void setParcela(Parcela parcela) { this.parcela = parcela; }
    
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    
    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
    
    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
    
    public String getConfirmadoPor() { return confirmadoPor; }
    public void setConfirmadoPor(String confirmadoPor) { this.confirmadoPor = confirmadoPor; }
}