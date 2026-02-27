package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.ProfessorDTO;
import br.com.arirang.plataforma.entity.Professor;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.ProfessorRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorService.class);

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public List<Professor> listarTodosProfessores() {
        try {
            return professorRepository.findAllWithTurma();
        } catch (Exception e) {
            logger.error("Erro ao listar todos os professores: ", e);
            throw new BusinessException("Erro ao listar professores: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Professor> buscarProfessorPorId(Long id) {
        try {
            return professorRepository.findByIdWithTurma(id);
        } catch (Exception e) {
            logger.error("Erro ao buscar professor com ID {}: ", id, e);
            throw new BusinessException("Erro ao buscar professor: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Professor> listarProfessoresPorTurma(Long turmaId) {
        try {
            return professorRepository.findByTurmaId(turmaId);
        } catch (Exception e) {
            logger.error("Erro ao listar professores por turma ID {}: ", turmaId, e);
            throw new BusinessException("Erro ao listar professores por turma: " + e.getMessage());
        }
    }

    @Transactional
    public Professor criarProfessor(ProfessorDTO professorDTO) {
        try {
            Professor professor = new Professor();
            professor.setNomeCompleto(professorDTO.nomeCompleto());
            professor.setEmail(professorDTO.email() != null && !professorDTO.email().trim().isEmpty() ? professorDTO.email() : null);
            professor.setCpf(professorDTO.cpf() != null && !professorDTO.cpf().trim().isEmpty() ? professorDTO.cpf() : null);
            professor.setRg(professorDTO.rg() != null && !professorDTO.rg().trim().isEmpty() ? professorDTO.rg() : null);
            // Converter string vazia para null para passar na validação
            String telefone = professorDTO.telefone();
            professor.setTelefone(telefone != null && !telefone.trim().isEmpty() ? telefone : null);
            professor.setDataNascimento(professorDTO.dataNascimento());
            professor.setCargo(professorDTO.cargo() != null && !professorDTO.cargo().trim().isEmpty() ? professorDTO.cargo() : null);
            professor.setFormacao(professorDTO.formacao() != null && !professorDTO.formacao().trim().isEmpty() ? professorDTO.formacao() : null);

            // Vincular múltiplas turmas
            if (professorDTO.turmaIds() != null && !professorDTO.turmaIds().isEmpty()) {
                List<Turma> turmas = new ArrayList<>();
                for (Long turmaId : professorDTO.turmaIds()) {
                    Turma turma = turmaRepository.findById(turmaId)
                            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));
                    turmas.add(turma);
                }
                professor.setTurmas(turmas);
            } else {
                professor.setTurmas(new ArrayList<>());
            }

            Professor savedProfessor = professorRepository.save(professor);
            logger.info("Professor criado com ID: {} - Nome: {}", savedProfessor.getId(), savedProfessor.getNomeCompleto());
            return savedProfessor;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao criar professor: ", e);
            throw new BusinessException("Erro ao criar professor: " + e.getMessage());
        }
    }

    @Transactional
    public Professor atualizarProfessor(Long id, ProfessorDTO professorDTO) {
        try {
            Professor professor = professorRepository.findByIdWithTurma(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado com ID: " + id));

            professor.setNomeCompleto(professorDTO.nomeCompleto());
            professor.setEmail(professorDTO.email() != null && !professorDTO.email().trim().isEmpty() ? professorDTO.email() : null);
            professor.setCpf(professorDTO.cpf() != null && !professorDTO.cpf().trim().isEmpty() ? professorDTO.cpf() : null);
            professor.setRg(professorDTO.rg() != null && !professorDTO.rg().trim().isEmpty() ? professorDTO.rg() : null);
            // Converter string vazia para null para passar na validação
            String telefone = professorDTO.telefone();
            professor.setTelefone(telefone != null && !telefone.trim().isEmpty() ? telefone : null);
            professor.setDataNascimento(professorDTO.dataNascimento());
            professor.setCargo(professorDTO.cargo() != null && !professorDTO.cargo().trim().isEmpty() ? professorDTO.cargo() : null);
            professor.setFormacao(professorDTO.formacao() != null && !professorDTO.formacao().trim().isEmpty() ? professorDTO.formacao() : null);

            // Atualizar múltiplas turmas
            if (professorDTO.turmaIds() != null && !professorDTO.turmaIds().isEmpty()) {
                List<Turma> turmas = new ArrayList<>();
                for (Long turmaId : professorDTO.turmaIds()) {
                    Turma turma = turmaRepository.findById(turmaId)
                            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));
                    turmas.add(turma);
                }
                professor.setTurmas(turmas);
            } else {
                professor.setTurmas(new ArrayList<>());
            }

            Professor updatedProfessor = professorRepository.save(professor);
            logger.info("Professor atualizado com ID: {} - Nome: {}", updatedProfessor.getId(), updatedProfessor.getNomeCompleto());
            return updatedProfessor;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao atualizar professor com ID {}: ", id, e);
            throw new BusinessException("Erro ao atualizar professor: " + e.getMessage());
        }
    }

    @Transactional
    public void deletarProfessor(Long id) {
        try {
            Professor professor = professorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado com ID: " + id));
            professorRepository.delete(professor);
            logger.info("Professor deletado com ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao deletar professor com ID {}: ", id, e);
            throw new BusinessException("Erro ao deletar professor: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProfessorDTO> buscarProfessorPorIdAsDTO(Long id) {
        try {
            return professorRepository.findByIdWithTurma(id)
                    .map(this::convertToDTO);
        } catch (Exception e) {
            logger.error("Erro ao buscar professor DTO com ID {}: ", id, e);
            throw new BusinessException("Erro ao buscar professor: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ProfessorDTO> listarTodosProfessoresAsDTO() {
        try {
            return professorRepository.findAllWithTurma().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar professores DTO: ", e);
            throw new BusinessException("Erro ao listar professores: " + e.getMessage());
        }
    }

    private ProfessorDTO convertToDTO(Professor professor) {
        List<Long> turmaIds = professor.getTurmas() != null 
                ? professor.getTurmas().stream().map(Turma::getId).collect(Collectors.toList())
                : new ArrayList<>();
        List<String> turmaNomes = professor.getTurmas() != null 
                ? professor.getTurmas().stream().map(Turma::getNomeTurma).collect(Collectors.toList())
                : new ArrayList<>();
        
        return new ProfessorDTO(
                professor.getId(),
                professor.getNomeCompleto(),
                professor.getDataNascimento(),
                professor.getRg(),
                professor.getCpf(),
                professor.getEmail(),
                professor.getTelefone(),
                professor.getCargo(),
                professor.getFormacao(),
                turmaIds,
                turmaNomes
        );
    }
}

