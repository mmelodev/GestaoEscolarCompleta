package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.AuditoriaTurma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaTurmaRepository extends JpaRepository<AuditoriaTurma, Long> {
    
    List<AuditoriaTurma> findByTurmaIdOrderByDataAlteracaoDesc(Long turmaId);
    
    Optional<AuditoriaTurma> findByProtocolo(String protocolo);
    
    @Query("SELECT COUNT(a) FROM AuditoriaTurma a WHERE a.turmaId = :turmaId AND DATE(a.dataAlteracao) = CURRENT_DATE")
    Long countAlteracoesHoje(@Param("turmaId") Long turmaId);
    
    @Query("SELECT a FROM AuditoriaTurma a WHERE a.turmaId = :turmaId ORDER BY a.dataAlteracao DESC")
    List<AuditoriaTurma> findHistoricoCompleto(@Param("turmaId") Long turmaId);
}

