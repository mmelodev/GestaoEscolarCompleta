package br.com.arirang.plataforma.entity;

import br.com.arirang.plataforma.enums.TipoPresenca;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade para registrar a frequência de alunos nas aulas
 */
@Entity
@Table(name = "frequencias", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "turma_id", "data_aula"}))
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @NotNull
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @NotNull
    private Turma turma;

    @Column(name = "data_aula", nullable = false)
    @NotNull
    private LocalDate dataAula;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_presenca", nullable = false, length = 20)
    @NotNull
    private TipoPresenca tipoPresenca;

    @Column(name = "observacao", length = 500)
    private String observacao;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @PrePersist
    protected void onCreate() {
        dataRegistro = LocalDateTime.now();
    }

    // Construtor padrão
    public Frequencia() {
    }

    // Construtor com parâmetros principais
    public Frequencia(Aluno aluno, Turma turma, LocalDate dataAula, TipoPresenca tipoPresenca) {
        this.aluno = aluno;
        this.turma = turma;
        this.dataAula = dataAula;
        this.tipoPresenca = tipoPresenca;
        this.dataRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public LocalDate getDataAula() {
        return dataAula;
    }

    public void setDataAula(LocalDate dataAula) {
        this.dataAula = dataAula;
    }

    public TipoPresenca getTipoPresenca() {
        return tipoPresenca;
    }

    public void setTipoPresenca(TipoPresenca tipoPresenca) {
        this.tipoPresenca = tipoPresenca;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }
}
