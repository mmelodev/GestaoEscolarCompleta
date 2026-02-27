package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.PadraoBoletimDTO;
import br.com.arirang.plataforma.entity.PadraoBoletim;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.repository.PadraoBoletimRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PadraoBoletimService {
    
    @Autowired
    private PadraoBoletimRepository padraoBoletimRepository;
    
    @Autowired
    private TurmaRepository turmaRepository;
    
    @Transactional
    public PadraoBoletim salvarOuAtualizarPadrao(Long turmaId, PadraoBoletimDTO padraoDTO) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + turmaId));
        
        Optional<PadraoBoletim> padraoExistente = padraoBoletimRepository.findByTurmaId(turmaId);
        
        PadraoBoletim padrao;
        if (padraoExistente.isPresent()) {
            padrao = padraoExistente.get();
        } else {
            padrao = new PadraoBoletim();
            padrao.setTurma(turma);
        }
        
        // Atualizar descrições
        padrao.setDescricaoExercicio(padraoDTO.descricaoExercicio());
        padrao.setDescricaoTrabalho(padraoDTO.descricaoTrabalho());
        padrao.setDescricaoAvaliacao(padraoDTO.descricaoAvaliacao());
        padrao.setDescricaoProducaoOral(padraoDTO.descricaoProducaoOral());
        padrao.setDescricaoProducaoEscrita(padraoDTO.descricaoProducaoEscrita());
        padrao.setDescricaoCompreensaoOral(padraoDTO.descricaoCompreensaoOral());
        padrao.setDescricaoCompreensaoEscrita(padraoDTO.descricaoCompreensaoEscrita());
        padrao.setDescricaoProvaFinal(padraoDTO.descricaoProvaFinal());
        padrao.setDescricaoPresenca(padraoDTO.descricaoPresenca());
        
        return padraoBoletimRepository.save(padrao);
    }
    
    @Transactional(readOnly = true)
    public Optional<PadraoBoletimDTO> buscarPadraoPorTurmaId(Long turmaId) {
        return padraoBoletimRepository.findByTurmaId(turmaId)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<PadraoBoletim> buscarPadraoPorTurmaIdEntity(Long turmaId) {
        return padraoBoletimRepository.findByTurmaId(turmaId);
    }
    
    @Transactional
    public void deletarPadrao(Long turmaId) {
        padraoBoletimRepository.findByTurmaId(turmaId)
                .ifPresent(padraoBoletimRepository::delete);
    }
    
    private PadraoBoletimDTO convertToDTO(PadraoBoletim padrao) {
        return new PadraoBoletimDTO(
                padrao.getId(),
                padrao.getTurma() != null ? padrao.getTurma().getId() : null,
                padrao.getTurma() != null ? padrao.getTurma().getNomeTurma() : null,
                padrao.getDescricaoExercicio(),
                padrao.getDescricaoTrabalho(),
                padrao.getDescricaoAvaliacao(),
                padrao.getDescricaoProducaoOral(),
                padrao.getDescricaoProducaoEscrita(),
                padrao.getDescricaoCompreensaoOral(),
                padrao.getDescricaoCompreensaoEscrita(),
                padrao.getDescricaoProvaFinal(),
                padrao.getDescricaoPresenca()
        );
    }
}
