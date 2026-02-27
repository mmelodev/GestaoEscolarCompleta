package br.com.arirang.plataforma.repository;

import br.com.arirang.plataforma.entity.Boletim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletimRepository extends JpaRepository<Boletim, Long> {
    
    @Query("SELECT b FROM Boletim b LEFT JOIN FETCH b.notas LEFT JOIN FETCH b.aluno LEFT JOIN FETCH b.turma WHERE b.aluno.id = :alunoId AND b.turma.id = :turmaId")
    Optional<Boletim> findByAlunoIdAndTurmaId(Long alunoId, Long turmaId);
    
    @Query("SELECT b FROM Boletim b LEFT JOIN FETCH b.notas LEFT JOIN FETCH b.aluno LEFT JOIN FETCH b.turma WHERE b.turma.id = :turmaId")
    List<Boletim> findByTurmaId(Long turmaId);
    
    @Query("SELECT b FROM Boletim b LEFT JOIN FETCH b.notas LEFT JOIN FETCH b.aluno LEFT JOIN FETCH b.turma WHERE b.aluno.id = :alunoId")
    List<Boletim> findByAlunoId(Long alunoId);
    
    @Query("SELECT COUNT(b) FROM Boletim b WHERE b.turma.id = :turmaId AND b.finalizado = true")
    Long countBoletinsFinalizadosByTurmaId(Long turmaId);
    
    @Query("SELECT COUNT(a) FROM Aluno a JOIN a.turmas t WHERE t.id = :turmaId")
    Long countAlunosByTurmaId(Long turmaId);
    
    @Query("SELECT COUNT(b) FROM Boletim b")
    Long countAllBoletins();
    
    @Query("SELECT COUNT(b) FROM Boletim b WHERE b.finalizado = false")
    Long countBoletinsPendentes();
    
    @Query("SELECT COUNT(b) FROM Boletim b WHERE b.finalizado = true")
    Long countBoletinsFinalizados();
    
    @Query("SELECT b FROM Boletim b LEFT JOIN FETCH b.notas LEFT JOIN FETCH b.aluno LEFT JOIN FETCH b.turma WHERE b.id = :id")
    Optional<Boletim> findByIdWithAlunoAndTurma(Long id);
}
