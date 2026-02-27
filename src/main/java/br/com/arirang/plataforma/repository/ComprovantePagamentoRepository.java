package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.ComprovantePagamento;
import br.com.arirang.plataforma.entity.FormaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprovantePagamentoRepository extends JpaRepository<ComprovantePagamento, Long> {
    
    // Buscar comprovantes por contrato
    List<ComprovantePagamento> findByContratoId(Long contratoId);
    
    // Buscar comprovantes por parcela
    List<ComprovantePagamento> findByParcelaId(Long parcelaId);
    
    // Buscar comprovantes por forma de pagamento
    List<ComprovantePagamento> findByFormaPagamento(FormaPagamento formaPagamento);
    
    // Buscar comprovantes validados
    @Query("SELECT c FROM ComprovantePagamento c WHERE c.validado = true")
    List<ComprovantePagamento> findComprovantesValidados();
    
    // Buscar comprovantes pendentes de validação
    @Query("SELECT c FROM ComprovantePagamento c WHERE c.validado = false")
    List<ComprovantePagamento> findComprovantesPendentesValidacao();
    
    // Buscar comprovantes por período
    @Query("SELECT c FROM ComprovantePagamento c WHERE c.dataPagamento BETWEEN :dataInicio AND :dataFim")
    List<ComprovantePagamento> findByPeriodo(@Param("dataInicio") LocalDate dataInicio, 
                                            @Param("dataFim") LocalDate dataFim);
    
    // Buscar comprovantes por número de documento
    Optional<ComprovantePagamento> findByNumeroDocumento(String numeroDocumento);
    
    // Calcular valor total de comprovantes por contrato
    @Query("SELECT SUM(c.valorPago) FROM ComprovantePagamento c WHERE c.contrato.id = :contratoId AND c.validado = true")
    Optional<Double> calcularValorTotalComprovantes(@Param("contratoId") Long contratoId);
    
    // Contar comprovantes por status de validação
    @Query("SELECT COUNT(c) FROM ComprovantePagamento c WHERE c.validado = :validado")
    Long countByValidado(@Param("validado") boolean validado);
    
    // Buscar comprovantes por banco
    @Query("SELECT c FROM ComprovantePagamento c WHERE c.banco = :banco")
    List<ComprovantePagamento> findByBanco(@Param("banco") String banco);
    
    // Buscar comprovantes por valor (aproximado)
    @Query("SELECT c FROM ComprovantePagamento c WHERE c.valorPago BETWEEN :valorMinimo AND :valorMaximo")
    List<ComprovantePagamento> findByValorRange(@Param("valorMinimo") Double valorMinimo, 
                                               @Param("valorMaximo") Double valorMaximo);
}
