package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    @Query("SELECT DISTINCT a FROM Aluno a LEFT JOIN FETCH a.turmas LEFT JOIN FETCH a.responsavel")
    List<Aluno> findAllWithTurmasAndResponsavel();

    @Query("SELECT DISTINCT a FROM Aluno a JOIN a.turmas t LEFT JOIN FETCH a.turmas LEFT JOIN FETCH a.responsavel WHERE t.id = :turmaId")
    List<Aluno> findAllByTurmaIdWithFetch(Long turmaId);

    @Query("SELECT a FROM Aluno a LEFT JOIN FETCH a.turmas WHERE a.id = :id")
    java.util.Optional<Aluno> findByIdWithTurmasAndProf(Long id);

    @Query("SELECT DISTINCT a FROM Aluno a LEFT JOIN FETCH a.turmas LEFT JOIN FETCH a.responsavel WHERE a.id = :id")
    java.util.Optional<Aluno> findByIdWithTurmasAndResponsavel(Long id);

    @Query("SELECT DISTINCT a FROM Aluno a JOIN a.turmas t WHERE t = :turma")
    List<Aluno> findByTurmasContaining(br.com.arirang.plataforma.entity.Turma turma);

    @Query("SELECT a FROM Aluno a WHERE DAY(a.dataNascimento) = DAY(CURRENT_DATE) AND MONTH(a.dataNascimento) = MONTH(CURRENT_DATE)")
    List<Aluno> findAniversariantesDoDia();
}