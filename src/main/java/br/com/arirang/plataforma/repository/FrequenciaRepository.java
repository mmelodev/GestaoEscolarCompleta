package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Frequencia;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.enums.TipoPresenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FrequenciaRepository extends JpaRepository<Frequencia, Long> {

    /**
     * Busca frequência por aluno, turma e data
     */
    Optional<Frequencia> findByAlunoAndTurmaAndDataAula(Aluno aluno, Turma turma, LocalDate dataAula);

    /**
     * Lista todas as frequências de um aluno em uma turma
     */
    List<Frequencia> findByAlunoAndTurmaOrderByDataAulaDesc(Aluno aluno, Turma turma);

    /**
     * Lista frequências de uma turma em uma data específica
     */
    List<Frequencia> findByTurmaAndDataAula(Turma turma, LocalDate dataAula);

    /**
     * Lista frequências de uma turma em um período
     */
    @Query("SELECT f FROM Frequencia f WHERE f.turma = :turma " +
           "AND f.dataAula BETWEEN :dataInicio AND :dataFim ORDER BY f.dataAula DESC, f.aluno.nomeCompleto")
    List<Frequencia> findByTurmaAndPeriodo(
            @Param("turma") Turma turma,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    /**
     * Conta total de presenças de um aluno em uma turma
     */
    @Query("SELECT COUNT(f) FROM Frequencia f WHERE f.aluno = :aluno AND f.turma = :turma " +
           "AND f.tipoPresenca = :tipoPresenca")
    Long countByAlunoAndTurmaAndTipoPresenca(
            @Param("aluno") Aluno aluno,
            @Param("turma") Turma turma,
            @Param("tipoPresenca") TipoPresenca tipoPresenca);

    /**
     * Conta total de aulas registradas para uma turma em um período
     */
    @Query("SELECT COUNT(DISTINCT f.dataAula) FROM Frequencia f WHERE f.turma = :turma " +
           "AND f.dataAula BETWEEN :dataInicio AND :dataFim")
    Long countAulasByTurmaAndPeriodo(
            @Param("turma") Turma turma,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    /**
     * Lista alunos com frequência abaixo do mínimo em uma turma
     */
    @Query("SELECT f.aluno, COUNT(CASE WHEN f.tipoPresenca = 'PRESENTE' THEN 1 END) as presencas, " +
           "COUNT(f) as totalAulas " +
           "FROM Frequencia f WHERE f.turma = :turma " +
           "AND f.dataAula BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY f.aluno " +
           "HAVING (COUNT(CASE WHEN f.tipoPresenca = 'PRESENTE' THEN 1 END) * 100.0 / COUNT(f)) < :percentualMinimo")
    List<Object[]> findAlunosComFrequenciaBaixa(
            @Param("turma") Turma turma,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("percentualMinimo") Double percentualMinimo);

    /**
     * Lista frequências de um aluno em todas as turmas em um período
     */
    @Query("SELECT f FROM Frequencia f WHERE f.aluno = :aluno " +
           "AND f.dataAula BETWEEN :dataInicio AND :dataFim ORDER BY f.dataAula DESC")
    List<Frequencia> findByAlunoAndPeriodo(
            @Param("aluno") Aluno aluno,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
}
