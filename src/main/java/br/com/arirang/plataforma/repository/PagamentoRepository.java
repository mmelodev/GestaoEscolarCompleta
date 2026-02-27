package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    /**
     * Busca pagamentos por receita
     */
    List<Pagamento> findByReceitaIdOrderByDataPagamentoDesc(Long receitaId);

    /**
     * Busca pagamentos por aluno
     */
    @Query("SELECT p FROM Pagamento p WHERE p.receita.aluno.id = :alunoId ORDER BY p.dataPagamento DESC")
    List<Pagamento> findByAlunoIdOrderByDataPagamentoDesc(@Param("alunoId") Long alunoId);

    /**
     * Busca pagamentos por período
     */
    @Query("SELECT p FROM Pagamento p WHERE p.dataPagamento BETWEEN :dataInicio AND :dataFim ORDER BY p.dataPagamento DESC")
    List<Pagamento> findPagamentosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Busca pagamentos por forma de pagamento
     */
    List<Pagamento> findByFormaPagamentoOrderByDataPagamentoDesc(String formaPagamento);

    /**
     * Soma pagamentos por período
     */
    @Query("SELECT SUM(p.valorPago) FROM Pagamento p WHERE p.dataPagamento BETWEEN :dataInicio AND :dataFim")
    Double sumPagamentosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Soma pagamentos por aluno e período
     */
    @Query("SELECT SUM(p.valorPago) FROM Pagamento p WHERE p.receita.aluno.id = :alunoId AND p.dataPagamento BETWEEN :dataInicio AND :dataFim")
    Double sumPagamentosByAlunoAndPeriodo(@Param("alunoId") Long alunoId, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    /**
     * Busca pagamentos com filtros
     */
    @Query("SELECT p FROM Pagamento p WHERE " +
           "(:alunoId IS NULL OR p.receita.aluno.id = :alunoId) AND " +
           "(:formaPagamento IS NULL OR p.formaPagamento = :formaPagamento) AND " +
           "(:dataInicio IS NULL OR p.dataPagamento >= :dataInicio) AND " +
           "(:dataFim IS NULL OR p.dataPagamento <= :dataFim) " +
           "ORDER BY p.dataPagamento DESC")
    List<Pagamento> findPagamentosWithFilters(@Param("alunoId") Long alunoId,
                                             @Param("formaPagamento") String formaPagamento,
                                             @Param("dataInicio") LocalDate dataInicio,
                                             @Param("dataFim") LocalDate dataFim);

    /**
     * Conta pagamentos por forma de pagamento
     */
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.formaPagamento = :formaPagamento")
    Long countPagamentosByFormaPagamento(@Param("formaPagamento") String formaPagamento);

    /**
     * Busca último pagamento do aluno
     */
    @Query("SELECT p FROM Pagamento p WHERE p.receita.aluno.id = :alunoId ORDER BY p.dataPagamento DESC LIMIT 1")
    Pagamento findUltimoPagamentoByAluno(@Param("alunoId") Long alunoId);
    
    /**
     * Busca pagamento por ID com todas as relações carregadas (receita, contrato, aluno, turma)
     */
    @Query("SELECT DISTINCT p FROM Pagamento p " +
           "LEFT JOIN FETCH p.receita r " +
           "LEFT JOIN FETCH r.contrato c " +
           "LEFT JOIN FETCH r.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE p.id = :id")
    Optional<Pagamento> findByIdWithRelations(@Param("id") Long id);
}
