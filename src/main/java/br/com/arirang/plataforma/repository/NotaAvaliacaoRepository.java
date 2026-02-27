package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.NotaAvaliacao;
import br.com.arirang.plataforma.entity.Avaliacao;
import br.com.arirang.plataforma.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaAvaliacaoRepository extends JpaRepository<NotaAvaliacao, Long> {
    
    List<NotaAvaliacao> findByAvaliacao(Avaliacao avaliacao);
    
    List<NotaAvaliacao> findByAvaliacaoOrderByAlunoNomeCompleto(Avaliacao avaliacao);
    
    Optional<NotaAvaliacao> findByAvaliacaoAndAluno(Avaliacao avaliacao, Aluno aluno);
    
    @Query("SELECT na FROM NotaAvaliacao na LEFT JOIN FETCH na.aluno LEFT JOIN FETCH na.avaliacao WHERE na.avaliacao.id = :avaliacaoId ORDER BY na.aluno.nomeCompleto")
    List<NotaAvaliacao> findByAvaliacaoIdWithAluno(@Param("avaliacaoId") Long avaliacaoId);
    
    @Query("SELECT na FROM NotaAvaliacao na LEFT JOIN FETCH na.avaliacao a LEFT JOIN FETCH a.turma WHERE na.aluno.id = :alunoId ORDER BY a.dataAvaliacao DESC")
    List<NotaAvaliacao> findByAlunoIdWithAvaliacaoAndTurma(@Param("alunoId") Long alunoId);
    
    @Query("SELECT COUNT(na) FROM NotaAvaliacao na WHERE na.avaliacao.id = :avaliacaoId AND na.presente = true")
    Long countPresentesByAvaliacaoId(@Param("avaliacaoId") Long avaliacaoId);
    
    @Query("SELECT AVG(na.valorNota) FROM NotaAvaliacao na WHERE na.avaliacao.id = :avaliacaoId AND na.presente = true AND na.valorNota IS NOT NULL")
    Double calcularMediaByAvaliacaoId(@Param("avaliacaoId") Long avaliacaoId);
    
    @Query("SELECT na FROM NotaAvaliacao na LEFT JOIN FETCH na.aluno a LEFT JOIN FETCH na.avaliacao av LEFT JOIN FETCH av.turma t WHERE t.id = :turmaId ORDER BY av.dataAvaliacao DESC, a.nomeCompleto")
    List<NotaAvaliacao> findByTurmaIdWithAlunoAndAvaliacao(@Param("turmaId") Long turmaId);
    
    void deleteByAvaliacao(Avaliacao avaliacao);
}
