package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.service.TurmaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turmas")
public class TurmaRestController {

    private static final Logger logger = LoggerFactory.getLogger(TurmaRestController.class);

    @Autowired
    private TurmaService turmaService;

    private TurmaDTO convertToDTO(Turma turma) {
        return new TurmaDTO(
                turma.getId(),
                turma.getNomeTurma(),
                turma.getIdioma(),
                turma.getProfessorResponsavel() != null ? turma.getProfessorResponsavel().getId() : null,
                turma.getNivelProficiencia(),
                turma.getDiaTurma(),
                turma.getTurno(),
                turma.getFormato(),
                turma.getModalidade(),
                turma.getRealizador(),
                turma.getHoraInicio(),
                turma.getHoraTermino(),
                turma.getAnoSemestre(),
                turma.getCargaHorariaTotal(),
                turma.getQuantidadeAulas(),
                turma.getCalendarioPdf(),
                turma.getInicioTurma(),
                turma.getTerminoTurma(),
                turma.getSituacaoTurma(),
                turma.getAlunos() != null ? turma.getAlunos().stream().map(Aluno::getId).collect(Collectors.toList()) : Collections.emptyList()
        );
    }

    @GetMapping
    public ResponseEntity<List<TurmaDTO>> listar() {
        List<TurmaDTO> turmas = turmaService.listarTodasTurmas().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(turmas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> buscar(@PathVariable Long id) {
        return turmaService.buscarTurmaPorIdAsDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TurmaDTO> criar(@Valid @RequestBody TurmaDTO dto) {
        try {
            Turma turmaSalva = turmaService.criarTurma(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(turmaSalva));
        } catch (Exception e) {
            logger.error("Erro ao criar turma via API", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TurmaDTO dto) {
        try {
            Turma turmaAtualizada = turmaService.atualizarTurma(id, dto);
            return ResponseEntity.ok(convertToDTO(turmaAtualizada));
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar turma {} via API: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro geral ao atualizar turma {} via API", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            turmaService.deletarTurma(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar turma {} via API", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}