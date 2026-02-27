package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_avaliacao")
public class NotaAvaliacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Avaliação é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;
    
    @NotNull(message = "Aluno é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;
    
    @Min(value = 0, message = "Nota deve ser no mínimo 0")
    @Max(value = 100, message = "Nota deve ser no máximo 100")
    @Column(name = "valor_nota")
    private Integer valorNota;
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "data_lancamento", nullable = false)
    private LocalDateTime dataLancamento;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "presente", nullable = false)
    private Boolean presente = true;
    
    // Construtores
    public NotaAvaliacao() {
        this.dataLancamento = LocalDateTime.now();
    }
    
    public NotaAvaliacao(Avaliacao avaliacao, Aluno aluno, Integer valorNota) {
        this();
        this.avaliacao = avaliacao;
        this.aluno = aluno;
        this.valorNota = valorNota;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Avaliacao getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Avaliacao avaliacao) { this.avaliacao = avaliacao; }
    
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    
    public Integer getValorNota() { return valorNota; }
    public void setValorNota(Integer valorNota) { this.valorNota = valorNota; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public LocalDateTime getDataLancamento() { return dataLancamento; }
    public void setDataLancamento(LocalDateTime dataLancamento) { this.dataLancamento = dataLancamento; }
    
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    
    public Boolean getPresente() { return presente; }
    public void setPresente(Boolean presente) { this.presente = presente; }
    
    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}