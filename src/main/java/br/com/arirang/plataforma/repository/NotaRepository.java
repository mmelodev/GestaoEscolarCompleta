package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    
    List<Nota> findByBoletimId(Long boletimId);
    
    void deleteByBoletimId(Long boletimId);
}
