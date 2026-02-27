package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "senhas_turma")
public class SenhaTurma {
    
    @Id
    @Column(name = "turma_id")
    private Long turmaId;
    
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;
    
    @Column(name = "senha_original", length = 50)
    private String senhaOriginal; // Armazenada apenas para exibição inicial (criptografada)
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;
    
    @Column(name = "ultima_tentativa")
    private LocalDateTime ultimaTentativa;
    
    @Column(name = "bloqueado")
    private Boolean bloqueado = false;
    
    @OneToOne
    @JoinColumn(name = "turma_id")
    @MapsId
    private Turma turma;
    
    public SenhaTurma() {
        this.dataCriacao = LocalDateTime.now();
    }
    
    public SenhaTurma(Long turmaId, String senhaHash) {
        this();
        this.turmaId = turmaId;
        this.senhaHash = senhaHash;
    }
    
    // Getters e Setters
    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }
    
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    
    public String getSenhaOriginal() { return senhaOriginal; }
    public void setSenhaOriginal(String senhaOriginal) { this.senhaOriginal = senhaOriginal; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public Integer getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(Integer tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }
    
    public LocalDateTime getUltimaTentativa() { return ultimaTentativa; }
    public void setUltimaTentativa(LocalDateTime ultimaTentativa) { this.ultimaTentativa = ultimaTentativa; }
    
    public Boolean getBloqueado() { return bloqueado; }
    public void setBloqueado(Boolean bloqueado) { this.bloqueado = bloqueado; }
    
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    
    public void incrementarTentativaFalha() {
        this.tentativasFalhas++;
        this.ultimaTentativa = LocalDateTime.now();
        if (this.tentativasFalhas >= 3) {
            this.bloqueado = true;
        }
    }
    
    public void resetarTentativas() {
        this.tentativasFalhas = 0;
        this.ultimaTentativa = null;
        this.bloqueado = false;
    }
}

