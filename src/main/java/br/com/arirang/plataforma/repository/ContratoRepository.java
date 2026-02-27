package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    /**
     * Busca contratos por aluno
     */
    List<Contrato> findByAlunoIdOrderByDataCriacaoDesc(Long alunoId);

    /**
     * Busca contratos por turma
     */
    List<Contrato> findByTurmaIdOrderByDataCriacaoDesc(Long turmaId);

    /**
     * Busca contrato ativo por aluno e turma
     */
    Optional<Contrato> findByAlunoIdAndTurmaIdAndSituacaoContrato(Long alunoId, Long turmaId, String situacao);

    /**
     * Busca contratos por situação
     */
    List<Contrato> findBySituacaoContratoOrderByDataCriacaoDesc(String situacao);

    /**
     * Verifica se existe contrato ativo para o aluno na turma
     */
    boolean existsByAlunoIdAndTurmaIdAndSituacaoContrato(Long alunoId, Long turmaId, String situacao);

    /**
     * Busca contratos por número do contrato
     */
    Optional<Contrato> findByNumeroContrato(String numeroContrato);

    /**
     * Busca contratos com filtros avançados (excluindo contratos com alunos ou turmas deletados)
     */
    @Query("SELECT c FROM Contrato c WHERE " +
           "c.aluno.id IS NOT NULL AND " +
           "c.turma.id IS NOT NULL AND " +
           "(:alunoId IS NULL OR c.aluno.id = :alunoId) AND " +
           "(:turmaId IS NULL OR c.turma.id = :turmaId) AND " +
           "(:situacao IS NULL OR c.situacaoContrato = :situacao) AND " +
           "(:numeroContrato IS NULL OR c.numeroContrato LIKE CONCAT('%', :numeroContrato, '%')) " +
           "ORDER BY c.dataCriacao DESC")
    List<Contrato> findContratosWithFilters(@Param("alunoId") Long alunoId,
                                           @Param("turmaId") Long turmaId,
                                           @Param("situacao") String situacao,
                                           @Param("numeroContrato") String numeroContrato);

    /**
     * Conta contratos ativos por turma
     */
    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.turma.id = :turmaId AND c.situacaoContrato = 'ATIVO'")
    Long countContratosAtivosByTurma(@Param("turmaId") Long turmaId);

    /**
     * Conta contratos criados entre duas datas (para geração de número único)
     * Usado para contar contratos do mês atual e evitar duplicatas
     */
    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.dataCriacao BETWEEN :inicio AND :fim")
    Long countByDataCriacaoBetween(@Param("inicio") LocalDateTime inicio, 
                                    @Param("fim") LocalDateTime fim);

    /**
     * Busca números de contrato do mês atual que seguem o padrão CTRYYYYMM####
     * Usado para gerar o próximo número sequencial único
     * Retorna ordenado do maior para o menor para facilitar encontrar o maior número
     */
    @Query("SELECT c.numeroContrato FROM Contrato c WHERE c.numeroContrato LIKE :prefixo ORDER BY c.numeroContrato DESC")
    List<String> findNumeroContratosByPrefixo(@Param("prefixo") String prefixo);
}
