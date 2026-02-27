package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Receita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    /**
     * Busca receitas por aluno
     */
    List<Receita> findByAlunoIdOrderByDataVencimentoDesc(Long alunoId);

    /**
     * Busca receitas por contrato
     */
    List<Receita> findByContratoIdOrderByDataVencimentoAsc(Long contratoId);

    /**
     * Busca receitas por situação
     */
    List<Receita> findBySituacaoOrderByDataVencimentoAsc(String situacao);

    /**
     * Busca receitas vencidas (excluindo alunos deletados)
     */
    @Query("SELECT r FROM Receita r WHERE r.dataVencimento < :dataAtual AND r.situacao = 'PENDENTE' AND r.aluno.id IS NOT NULL ORDER BY r.dataVencimento ASC")
    List<Receita> findReceitasVencidas(@Param("dataAtual") LocalDate dataAtual);

    /**
     * Busca receitas por período
     */
    @Query("SELECT r FROM Receita r WHERE r.dataVencimento BETWEEN :dataInicio AND :dataFim ORDER BY r.dataVencimento ASC")
    List<Receita> findReceitasPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Busca receitas com filtros avançados
     */
    @Query("SELECT r FROM Receita r WHERE " +
           "(:alunoId IS NULL OR r.aluno.id = :alunoId) AND " +
           "(:contratoId IS NULL OR r.contrato.id = :contratoId) AND " +
           "(:situacao IS NULL OR r.situacao = :situacao) AND " +
           "(:tipoReceita IS NULL OR r.tipoReceita = :tipoReceita) AND " +
           "(:dataInicio IS NULL OR r.dataVencimento >= :dataInicio) AND " +
           "(:dataFim IS NULL OR r.dataVencimento <= :dataFim) " +
           "ORDER BY r.dataVencimento ASC")
    List<Receita> findReceitasWithFilters(@Param("alunoId") Long alunoId,
                                         @Param("contratoId") Long contratoId,
                                         @Param("situacao") String situacao,
                                         @Param("tipoReceita") String tipoReceita,
                                         @Param("dataInicio") LocalDate dataInicio,
                                         @Param("dataFim") LocalDate dataFim);

    /**
     * Soma receitas por situação (excluindo alunos deletados)
     */
    @Query("SELECT SUM(r.valorFinal) FROM Receita r WHERE r.situacao = :situacao AND r.aluno.id IS NOT NULL")
    Optional<Double> sumReceitasBySituacao(@Param("situacao") String situacao);

    /**
     * Soma receitas por aluno e situação
     */
    @Query("SELECT SUM(r.valorFinal) FROM Receita r WHERE r.aluno.id = :alunoId AND r.situacao = :situacao")
    Optional<Double> sumReceitasByAlunoAndSituacao(@Param("alunoId") Long alunoId, @Param("situacao") String situacao);

    /**
     * Conta receitas por situação (excluindo alunos deletados)
     */
    @Query("SELECT COUNT(r) FROM Receita r WHERE r.situacao = :situacao AND r.aluno.id IS NOT NULL")
    Long countReceitasBySituacao(@Param("situacao") String situacao);

    /**
     * Busca próxima receita vencida do aluno
     */
    @Query("SELECT r FROM Receita r WHERE r.aluno.id = :alunoId AND r.situacao = 'PENDENTE' ORDER BY r.dataVencimento ASC")
    Optional<Receita> findProximaReceitaVencida(@Param("alunoId") Long alunoId);

    /**
     * Busca receitas com pagamentos parciais
     */
    @Query("SELECT r FROM Receita r WHERE r.situacao = 'PARCIAL' ORDER BY r.dataVencimento ASC")
    List<Receita> findReceitasComPagamentosParciais();
}
