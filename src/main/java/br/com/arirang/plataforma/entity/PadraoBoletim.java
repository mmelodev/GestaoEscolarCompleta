package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "padroes_boletim")
public class PadraoBoletim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false, unique = true)
    private Turma turma;
    
    // Descrições padrão para cada tipo de nota
    @Column(name = "descricao_exercicio", length = 500)
    private String descricaoExercicio;
    
    @Column(name = "descricao_trabalho", length = 500)
    private String descricaoTrabalho;
    
    @Column(name = "descricao_avaliacao", length = 500)
    private String descricaoAvaliacao;
    
    @Column(name = "descricao_producao_oral", length = 500)
    private String descricaoProducaoOral;
    
    @Column(name = "descricao_producao_escrita", length = 500)
    private String descricaoProducaoEscrita;
    
    @Column(name = "descricao_compreensao_oral", length = 500)
    private String descricaoCompreensaoOral;
    
    @Column(name = "descricao_compreensao_escrita", length = 500)
    private String descricaoCompreensaoEscrita;
    
    @Column(name = "descricao_prova_final", length = 500)
    private String descricaoProvaFinal;
    
    @Column(name = "descricao_presenca", length = 500)
    private String descricaoPresenca;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    // Construtor padrão
    public PadraoBoletim() {
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
    
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    
    public String getDescricaoExercicio() { return descricaoExercicio; }
    public void setDescricaoExercicio(String descricaoExercicio) { this.descricaoExercicio = descricaoExercicio; }
    
    public String getDescricaoTrabalho() { return descricaoTrabalho; }
    public void setDescricaoTrabalho(String descricaoTrabalho) { this.descricaoTrabalho = descricaoTrabalho; }
    
    public String getDescricaoAvaliacao() { return descricaoAvaliacao; }
    public void setDescricaoAvaliacao(String descricaoAvaliacao) { this.descricaoAvaliacao = descricaoAvaliacao; }
    
    public String getDescricaoProducaoOral() { return descricaoProducaoOral; }
    public void setDescricaoProducaoOral(String descricaoProducaoOral) { this.descricaoProducaoOral = descricaoProducaoOral; }
    
    public String getDescricaoProducaoEscrita() { return descricaoProducaoEscrita; }
    public void setDescricaoProducaoEscrita(String descricaoProducaoEscrita) { this.descricaoProducaoEscrita = descricaoProducaoEscrita; }
    
    public String getDescricaoCompreensaoOral() { return descricaoCompreensaoOral; }
    public void setDescricaoCompreensaoOral(String descricaoCompreensaoOral) { this.descricaoCompreensaoOral = descricaoCompreensaoOral; }
    
    public String getDescricaoCompreensaoEscrita() { return descricaoCompreensaoEscrita; }
    public void setDescricaoCompreensaoEscrita(String descricaoCompreensaoEscrita) { this.descricaoCompreensaoEscrita = descricaoCompreensaoEscrita; }
    
    public String getDescricaoProvaFinal() { return descricaoProvaFinal; }
    public void setDescricaoProvaFinal(String descricaoProvaFinal) { this.descricaoProvaFinal = descricaoProvaFinal; }
    
    public String getDescricaoPresenca() { return descricaoPresenca; }
    public void setDescricaoPresenca(String descricaoPresenca) { this.descricaoPresenca = descricaoPresenca; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    
    /**
     * Obtém a descrição padrão para um tipo de nota específico
     */
    public String getDescricaoPorTipo(TipoNota tipoNota) {
        if (tipoNota == null) return null;
        
        return switch (tipoNota) {
            case EXERCICIO -> descricaoExercicio;
            case TRABALHO -> descricaoTrabalho;
            case AVALIACAO -> descricaoAvaliacao;
            case PRODUCAO_ORAL -> descricaoProducaoOral;
            case PRODUCAO_ESCRITA -> descricaoProducaoEscrita;
            case COMPREENSAO_ORAL -> descricaoCompreensaoOral;
            case COMPREENSAO_ESCRITA -> descricaoCompreensaoEscrita;
            case PROVA_FINAL -> descricaoProvaFinal;
            case PRESENCA -> descricaoPresenca;
        };
    }
    
    /**
     * Define a descrição padrão para um tipo de nota específico
     */
    public void setDescricaoPorTipo(TipoNota tipoNota, String descricao) {
        if (tipoNota == null) return;
        
        switch (tipoNota) {
            case EXERCICIO -> descricaoExercicio = descricao;
            case TRABALHO -> descricaoTrabalho = descricao;
            case AVALIACAO -> descricaoAvaliacao = descricao;
            case PRODUCAO_ORAL -> descricaoProducaoOral = descricao;
            case PRODUCAO_ESCRITA -> descricaoProducaoEscrita = descricao;
            case COMPREENSAO_ORAL -> descricaoCompreensaoOral = descricao;
            case COMPREENSAO_ESCRITA -> descricaoCompreensaoEscrita = descricao;
            case PROVA_FINAL -> descricaoProvaFinal = descricao;
            case PRESENCA -> descricaoPresenca = descricao;
        }
    }
}
