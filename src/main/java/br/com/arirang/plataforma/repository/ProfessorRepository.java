package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Query("SELECT DISTINCT p FROM Professor p LEFT JOIN FETCH p.turmas")
    List<Professor> findAllWithTurma();

    @Query("SELECT DISTINCT p FROM Professor p LEFT JOIN FETCH p.turmas WHERE p.id = :id")
    Optional<Professor> findByIdWithTurma(Long id);

    @Query("SELECT DISTINCT p FROM Professor p LEFT JOIN FETCH p.turmas t WHERE t.id = :turmaId")
    List<Professor> findByTurmaId(Long turmaId);
}

