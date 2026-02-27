package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da avaliação é obrigatório")
    @Column(name = "nome_avaliacao", nullable = false, length = 200)
    private String nomeAvaliacao;
    
    @Column(name = "descricao", length = 1000)
    private String descricao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_avaliacao", nullable = false)
    private TipoNota tipoAvaliacao;
    
    @NotNull(message = "Turma é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;
    
    @NotNull(message = "Data da avaliação é obrigatória")
    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "peso", nullable = false)
    private Integer peso = 1;
    
    @Column(name = "valor_maximo", nullable = false)
    private Integer valorMaximo = 100;
    
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;
    
    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotaAvaliacao> notas;
    
    // Construtores
    public Avaliacao() {
        this.dataCriacao = LocalDateTime.now();
    }
    
    public Avaliacao(String nomeAvaliacao, TipoNota tipoAvaliacao, Turma turma, LocalDate dataAvaliacao) {
        this();
        this.nomeAvaliacao = nomeAvaliacao;
        this.tipoAvaliacao = tipoAvaliacao;
        this.turma = turma;
        this.dataAvaliacao = dataAvaliacao;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomeAvaliacao() { return nomeAvaliacao; }
    public void setNomeAvaliacao(String nomeAvaliacao) { this.nomeAvaliacao = nomeAvaliacao; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public TipoNota getTipoAvaliacao() { return tipoAvaliacao; }
    public void setTipoAvaliacao(TipoNota tipoAvaliacao) { this.tipoAvaliacao = tipoAvaliacao; }
    
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDate dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    
    public Integer getPeso() { return peso; }
    public void setPeso(Integer peso) { this.peso = peso; }
    
    public Integer getValorMaximo() { return valorMaximo; }
    public void setValorMaximo(Integer valorMaximo) { this.valorMaximo = valorMaximo; }
    
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    
    public List<NotaAvaliacao> getNotas() { return notas; }
    public void setNotas(List<NotaAvaliacao> notas) { this.notas = notas; }
    
    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}