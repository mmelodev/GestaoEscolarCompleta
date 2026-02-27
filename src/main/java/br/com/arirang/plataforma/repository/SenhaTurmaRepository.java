package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.SenhaTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SenhaTurmaRepository extends JpaRepository<SenhaTurma, Long> {
    
    Optional<SenhaTurma> findByTurmaId(Long turmaId);
    
    boolean existsByTurmaId(Long turmaId);
}

