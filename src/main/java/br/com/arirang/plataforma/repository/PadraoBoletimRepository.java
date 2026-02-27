package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.PadraoBoletim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PadraoBoletimRepository extends JpaRepository<PadraoBoletim, Long> {
    
    @Query("SELECT p FROM PadraoBoletim p LEFT JOIN FETCH p.turma WHERE p.turma.id = :turmaId")
    Optional<PadraoBoletim> findByTurmaId(@Param("turmaId") Long turmaId);
    
    boolean existsByTurmaId(Long turmaId);
}
