package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.ConfiguracaoUsuario;
import br.com.arirang.plataforma.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoUsuarioRepository extends JpaRepository<ConfiguracaoUsuario, Long> {
    
    /**
     * Busca configuração por usuário
     */
    Optional<ConfiguracaoUsuario> findByUsuario(Usuario usuario);
    
    /**
     * Busca configuração por ID do usuário
     */
    Optional<ConfiguracaoUsuario> findByUsuarioId(Long usuarioId);
    
    /**
     * Verifica se existe configuração para o usuário
     */
    boolean existsByUsuario(Usuario usuario);
}
