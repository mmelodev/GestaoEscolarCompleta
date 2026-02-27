package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.CategoriaFinanceira;
import br.com.arirang.plataforma.entity.Financeiro;
import br.com.arirang.plataforma.entity.TipoMovimentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceiroRepository extends JpaRepository<Financeiro, Long> {
    
    // Buscar movimentos por período
    List<Financeiro> findByDataMovimentoBetween(LocalDate dataInicio, LocalDate dataFim);
    
    // Buscar movimentos por tipo e período
    List<Financeiro> findByTipoMovimentoAndDataMovimentoBetween(
            TipoMovimentoFinanceiro tipoMovimento, LocalDate dataInicio, LocalDate dataFim);
    
    // Buscar movimentos por aluno
    List<Financeiro> findByAlunoId(Long alunoId);
    
    // Buscar movimentos por contrato
    List<Financeiro> findByContratoId(Long contratoId);
    
    // Buscar movimentos por parcela
    List<Financeiro> findByParcelaId(Long parcelaId);
    
    // Buscar movimentos por categoria
    List<Financeiro> findByCategoria(CategoriaFinanceira categoria);
    
    // Buscar movimentos por tipo
    List<Financeiro> findByTipoMovimento(TipoMovimentoFinanceiro tipoMovimento);
    
    // Buscar movimentos pendentes de confirmação
    List<Financeiro> findByConfirmadoFalse();
    
    // Buscar movimentos confirmados
    List<Financeiro> findByConfirmadoTrue();
    
    // Buscar movimentos por número de documento
    Optional<Financeiro> findByNumeroDocumento(String numeroDocumento);
    
    // Buscar movimentos por referência
    List<Financeiro> findByReferencia(String referencia);
    
    // Calcular valor total de receitas por período
    @Query("SELECT SUM(f.valor) FROM Financeiro f WHERE f.tipoMovimento = 'RECEITA' AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    Optional<BigDecimal> calcularReceitaTotalPorPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                                       @Param("dataFim") LocalDate dataFim);
    
    // Calcular valor total de despesas por período
    @Query("SELECT SUM(f.valor) FROM Financeiro f WHERE f.tipoMovimento = 'DESPESA' AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    Optional<BigDecimal> calcularDespesaTotalPorPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                                        @Param("dataFim") LocalDate dataFim);
    
    // Calcular valor total por categoria e período
    @Query("SELECT SUM(f.valor) FROM Financeiro f WHERE f.categoria = :categoria AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    Optional<BigDecimal> calcularValorTotalPorCategoriaEPeriodo(@Param("categoria") CategoriaFinanceira categoria,
                                                                @Param("dataInicio") LocalDate dataInicio,
                                                                @Param("dataFim") LocalDate dataFim);
    
    // Calcular valor total de receitas por aluno
    @Query("SELECT SUM(f.valor) FROM Financeiro f WHERE f.tipoMovimento = 'RECEITA' AND f.aluno.id = :alunoId")
    Optional<BigDecimal> calcularReceitaTotalPorAluno(@Param("alunoId") Long alunoId);
    
    // Calcular valor total de despesas por aluno
    @Query("SELECT SUM(f.valor) FROM Financeiro f WHERE f.tipoMovimento = 'DESPESA' AND f.aluno.id = :alunoId")
    Optional<BigDecimal> calcularDespesaTotalPorAluno(@Param("alunoId") Long alunoId);
    
    // Buscar movimentos por aluno e período
    @Query("SELECT f FROM Financeiro f WHERE f.aluno.id = :alunoId AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    List<Financeiro> findByAlunoIdAndPeriodo(@Param("alunoId") Long alunoId,
                                            @Param("dataInicio") LocalDate dataInicio,
                                            @Param("dataFim") LocalDate dataFim);
    
    // Buscar movimentos por contrato e período
    @Query("SELECT f FROM Financeiro f WHERE f.contrato.id = :contratoId AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    List<Financeiro> findByContratoIdAndPeriodo(@Param("contratoId") Long contratoId,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);
    
    // Contar movimentos por tipo
    @Query("SELECT COUNT(f) FROM Financeiro f WHERE f.tipoMovimento = :tipoMovimento")
    Long countByTipoMovimento(@Param("tipoMovimento") TipoMovimentoFinanceiro tipoMovimento);
    
    // Contar movimentos por categoria
    @Query("SELECT COUNT(f) FROM Financeiro f WHERE f.categoria = :categoria")
    Long countByCategoria(@Param("categoria") CategoriaFinanceira categoria);
    
    // Buscar movimentos por valor (range)
    @Query("SELECT f FROM Financeiro f WHERE f.valor BETWEEN :valorMinimo AND :valorMaximo")
    List<Financeiro> findByValorRange(@Param("valorMinimo") BigDecimal valorMinimo,
                                     @Param("valorMaximo") BigDecimal valorMaximo);
    
    // Buscar movimentos confirmados por período
    @Query("SELECT f FROM Financeiro f WHERE f.confirmado = true AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    List<Financeiro> findConfirmadosPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                                              @Param("dataFim") LocalDate dataFim);
    
    // Buscar movimentos pendentes por período
    @Query("SELECT f FROM Financeiro f WHERE f.confirmado = false AND f.dataMovimento BETWEEN :dataInicio AND :dataFim")
    List<Financeiro> findPendentesPorPeriodo(@Param("dataInicio") LocalDate dataInicio,
                                             @Param("dataFim") LocalDate dataFim);
}
