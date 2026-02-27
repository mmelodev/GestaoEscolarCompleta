package br.com.arirang.plataforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.arirang.plataforma.entity.Turma;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    
    @Query("SELECT DISTINCT t FROM Turma t LEFT JOIN FETCH t.alunos LEFT JOIN FETCH t.professorResponsavel WHERE t.id = :id")
    Optional<Turma> findByIdWithAlunos(@Param("id") Long id);
    
    @Query("SELECT DISTINCT t FROM Turma t LEFT JOIN FETCH t.alunos LEFT JOIN FETCH t.professorResponsavel")
    java.util.List<Turma> findAllWithAlunos();
    
    @Query("SELECT t FROM Turma t WHERE LOWER(TRIM(t.nomeTurma)) = LOWER(TRIM(:nomeTurma))")
    java.util.List<Turma> findByNomeTurmaIgnoreCaseAndTrim(@Param("nomeTurma") String nomeTurma);
}