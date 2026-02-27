package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.ConfiguracaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoFinanceiraRepository extends JpaRepository<ConfiguracaoFinanceira, Long> {

    /**
     * Busca configuração por chave
     */
    Optional<ConfiguracaoFinanceira> findByChaveAndAtivoTrue(String chave);

    /**
     * Busca todas as configurações ativas
     */
    java.util.List<ConfiguracaoFinanceira> findByAtivoTrueOrderByChave();

    /**
     * Busca configurações por tipo
     */
    java.util.List<ConfiguracaoFinanceira> findByTipoAndAtivoTrueOrderByChave(String tipo);

    /**
     * Verifica se configuração existe
     */
    boolean existsByChave(String chave);
}
