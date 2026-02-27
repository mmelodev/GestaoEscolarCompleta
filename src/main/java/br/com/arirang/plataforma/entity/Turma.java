package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import br.com.arirang.plataforma.enums.Turno;
import br.com.arirang.plataforma.enums.Formato;
import br.com.arirang.plataforma.enums.Modalidade;
import br.com.arirang.plataforma.converter.ModalidadeConverter;
import br.com.arirang.plataforma.converter.TurnoConverter;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_turma")
    private String nomeTurma;

    @Column(length = 50)
    private String idioma;

    private String nivelProficiencia;
    private String diaTurma;
    @Convert(converter = TurnoConverter.class)
    @Column(name = "turno")
    private Turno turno;
    
    @Enumerated(EnumType.STRING)
    private Formato formato;
    
    @Convert(converter = ModalidadeConverter.class)
    @Column(name = "modalidade")
    private Modalidade modalidade;
    private String realizador;
    private String horaInicio;
    private String horaTermino;
    private String anoSemestre;
    private Integer cargaHorariaTotal;
    private Integer quantidadeAulas;
    @Column(name = "calendario_pdf", length = 255)
    private String calendarioPdf;
    private LocalDate inicioTurma;    // Alterado de LocalDateTime para LocalDate
    private LocalDate terminoTurma;   // Alterado de LocalDateTime para LocalDate
    private String situacaoTurma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_responsavel_id")
    private Professor professorResponsavel;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "aluno_turma",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id")
    )
    private List<Aluno> alunos;

    // Getters e Setters (ajustados)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getNivelProficiencia() { return nivelProficiencia; }
    public void setNivelProficiencia(String nivelProficiencia) { this.nivelProficiencia = nivelProficiencia; }
    public String getDiaTurma() { return diaTurma; }
    public void setDiaTurma(String diaTurma) { this.diaTurma = diaTurma; }
    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }
    public Formato getFormato() { return formato; }
    public void setFormato(Formato formato) { this.formato = formato; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public String getRealizador() { return realizador; }
    public void setRealizador(String realizador) { this.realizador = realizador; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraTermino() { return horaTermino; }
    public void setHoraTermino(String horaTermino) { this.horaTermino = horaTermino; }
    public String getAnoSemestre() { return anoSemestre; }
    public void setAnoSemestre(String anoSemestre) { this.anoSemestre = anoSemestre; }
    public Integer getCargaHorariaTotal() { return cargaHorariaTotal; }
    public void setCargaHorariaTotal(Integer cargaHorariaTotal) { this.cargaHorariaTotal = cargaHorariaTotal; }
    public Integer getQuantidadeAulas() { return quantidadeAulas; }
    public void setQuantidadeAulas(Integer quantidadeAulas) { this.quantidadeAulas = quantidadeAulas; }
    public String getCalendarioPdf() { return calendarioPdf; }
    public void setCalendarioPdf(String calendarioPdf) { this.calendarioPdf = calendarioPdf; }
    public LocalDate getInicioTurma() { return inicioTurma; }
    public void setInicioTurma(LocalDate inicioTurma) { this.inicioTurma = inicioTurma; }
    public LocalDate getTerminoTurma() { return terminoTurma; }
    public void setTerminoTurma(LocalDate terminoTurma) { this.terminoTurma = terminoTurma; }
    public String getSituacaoTurma() { return situacaoTurma; }
    public void setSituacaoTurma(String situacaoTurma) { this.situacaoTurma = situacaoTurma; }
    public Professor getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(Professor professorResponsavel) { this.professorResponsavel = professorResponsavel; }
    public List<Aluno> getAlunos() { return alunos; }
    public void setAlunos(List<Aluno> alunos) { this.alunos = alunos; }
}