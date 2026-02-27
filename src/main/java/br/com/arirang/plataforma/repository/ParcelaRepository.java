package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.StatusParcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    
    // Buscar parcelas por contrato
    List<Parcela> findByContratoId(Long contratoId);
    
    // Buscar parcelas por status
    List<Parcela> findByStatusParcela(StatusParcela statusParcela);
    
    // Buscar parcelas pendentes
    @Query("SELECT p FROM Parcela p WHERE p.statusParcela = 'PENDENTE'")
    List<Parcela> findParcelasPendentes();
    
    // Buscar parcelas vencidas
    @Query("SELECT p FROM Parcela p WHERE p.dataVencimento < :dataAtual AND p.statusParcela = 'PENDENTE'")
    List<Parcela> findParcelasVencidas(@Param("dataAtual") LocalDate dataAtual);
    
    // Buscar parcelas em atraso
    @Query("SELECT p FROM Parcela p WHERE p.dataVencimento < :dataAtual AND p.statusParcela = 'EM_ATRASO'")
    List<Parcela> findParcelasEmAtraso(@Param("dataAtual") LocalDate dataAtual);
    
    // Buscar próxima parcela a vencer por contrato
    @Query("SELECT p FROM Parcela p WHERE p.contrato.id = :contratoId AND p.statusParcela = 'PENDENTE' ORDER BY p.dataVencimento ASC")
    Optional<Parcela> findProximaParcelaAVencer(@Param("contratoId") Long contratoId);
    
    // Buscar parcelas por período de vencimento
    @Query("SELECT p FROM Parcela p WHERE p.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<Parcela> findByPeriodoVencimento(@Param("dataInicio") LocalDate dataInicio, 
                                          @Param("dataFim") LocalDate dataFim);
    
    // Contar parcelas por status
    @Query("SELECT COUNT(p) FROM Parcela p WHERE p.statusParcela = :status")
    Long countByStatusParcela(@Param("status") StatusParcela status);
    
    // Calcular valor total de parcelas pendentes por contrato
    @Query("SELECT SUM(p.valorParcela) FROM Parcela p WHERE p.contrato.id = :contratoId AND p.statusParcela = 'PENDENTE'")
    Optional<Double> calcularValorTotalParcelasPendentes(@Param("contratoId") Long contratoId);
    
    // Buscar parcelas pagas por período
    @Query("SELECT p FROM Parcela p WHERE p.dataPagamento BETWEEN :dataInicio AND :dataFim AND p.statusParcela = 'PAGA'")
    List<Parcela> findParcelasPagasPorPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                             @Param("dataFim") LocalDate dataFim);
    
    // Buscar parcelas por contrato ordenadas por data de vencimento
    List<Parcela> findByContratoIdOrderByDataVencimentoAsc(Long contratoId);
    
    // Buscar parcelas não pagas por contrato ordenadas por data de vencimento
    List<Parcela> findByContratoIdAndStatusParcelaOrderByDataVencimentoAsc(Long contratoId, StatusParcela statusParcela);
    
    // Buscar todas as parcelas com aluno e contrato (excluindo alunos deletados)
    @Query("SELECT DISTINCT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE a IS NOT NULL " +
           "ORDER BY p.dataVencimento DESC")
    List<Parcela> findAllWithAlunoAndContrato();
    
    // Buscar parcelas por status com aluno e contrato (excluindo alunos deletados)
    @Query("SELECT DISTINCT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE a IS NOT NULL AND p.statusParcela = :status " +
           "ORDER BY p.dataVencimento DESC")
    List<Parcela> findByStatusParcelaWithAlunoAndContrato(@Param("status") StatusParcela status);
    
    // Buscar parcelas vencidas com aluno e contrato (excluindo alunos deletados)
    @Query("SELECT DISTINCT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE a IS NOT NULL AND p.dataVencimento < :dataAtual AND p.statusParcela IN ('PENDENTE', 'EM_ATRASO') " +
           "ORDER BY p.dataVencimento ASC")
    List<Parcela> findParcelasVencidasWithAlunoAndContrato(@Param("dataAtual") LocalDate dataAtual);
    
    // Buscar parcelas por período com aluno e contrato (excluindo alunos deletados)
    @Query("SELECT DISTINCT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE a IS NOT NULL AND p.dataVencimento BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY p.dataVencimento DESC")
    List<Parcela> findByPeriodoVencimentoWithAlunoAndContrato(@Param("dataInicio") LocalDate dataInicio, 
                                                               @Param("dataFim") LocalDate dataFim);
    
    // Buscar parcela por ID com contrato e aluno carregados
    @Query("SELECT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE p.id = :id")
    Optional<Parcela> findByIdWithContrato(@Param("id") Long id);
    
    // Buscar parcelas pendentes vencidas, vencendo hoje ou nos próximos 5 dias com aluno e contrato
    @Query("SELECT DISTINCT p FROM Parcela p " +
           "LEFT JOIN FETCH p.contrato c " +
           "LEFT JOIN FETCH c.aluno a " +
           "LEFT JOIN FETCH c.turma t " +
           "WHERE a IS NOT NULL " +
           "AND p.statusParcela IN :statuses " +
           "AND (p.dataVencimento < :dataAtual OR p.dataVencimento BETWEEN :dataAtual AND :dataFim) " +
           "ORDER BY p.dataVencimento ASC")
    List<Parcela> findParcelasProximasAVencer(@Param("dataAtual") LocalDate dataAtual, 
                                               @Param("dataFim") LocalDate dataFim,
                                               @Param("statuses") java.util.List<StatusParcela> statuses);
}
