package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Avaliacao;
import br.com.arirang.plataforma.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    
    List<Avaliacao> findByTurmaOrderByDataAvaliacaoDesc(Turma turma);
    
    List<Avaliacao> findByTurmaAndAtivaTrueOrderByDataAvaliacaoDesc(Turma turma);
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.notas WHERE a.id = :id")
    Optional<Avaliacao> findByIdWithNotas(@Param("id") Long id);
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.turma WHERE a.id = :id")
    Optional<Avaliacao> findByIdWithTurma(@Param("id") Long id);
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.turma t LEFT JOIN FETCH a.notas WHERE a.ativa = true ORDER BY a.dataAvaliacao DESC")
    List<Avaliacao> findAllAtivasWithTurmaAndNotas();
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.turma t WHERE a.ativa = true AND t.id = :turmaId ORDER BY a.dataAvaliacao DESC")
    List<Avaliacao> findByTurmaIdAndAtivaTrue(@Param("turmaId") Long turmaId);
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.turma t WHERE a.dataAvaliacao BETWEEN :dataInicio AND :dataFim ORDER BY a.dataAvaliacao DESC")
    List<Avaliacao> findByDataAvaliacaoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.turma.id = :turmaId AND a.ativa = true")
    Long countByTurmaIdAndAtivaTrue(@Param("turmaId") Long turmaId);
    
    @Query("SELECT a FROM Avaliacao a LEFT JOIN FETCH a.turma t WHERE a.nomeAvaliacao LIKE %:nome% AND a.ativa = true ORDER BY a.dataAvaliacao DESC")
    List<Avaliacao> findByNomeAvaliacaoContainingIgnoreCaseAndAtivaTrue(@Param("nome") String nome);

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.ativa = true")
    Long countByAtivaTrue();

    @Query("SELECT COUNT(a) FROM Avaliacao a WHERE a.ativa = false")
    Long countByAtivaFalse();
}
