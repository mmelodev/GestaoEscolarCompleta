package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.service.AlunoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alunos")
public class AlunoRestController {

    private static final Logger logger = LoggerFactory.getLogger(AlunoRestController.class);

    @Autowired
    private AlunoService alunoService;

    private AlunoDTO convertToDTO(Aluno aluno) {
        return new AlunoDTO(
            aluno.getId(),
            aluno.getNomeCompleto(),
            aluno.getEmail(),
            aluno.getCpf(),
            aluno.getRg(),
            aluno.getOrgaoExpeditorRg(),
            aluno.getNacionalidade(),
            aluno.getUf(),
            aluno.getTelefone(),
            aluno.getDataNascimento(),
            aluno.getNomeSocial(),
            aluno.getApelido(),
            aluno.getGenero(),
            aluno.getSituacao(),
            aluno.getUltimoNivel(),
            aluno.getEndereco(),
            aluno.getGrauParentesco(),
            aluno.isResponsavelFinanceiro(),
            aluno.getResponsavel() != null ? aluno.getResponsavel().getNomeCompleto() : null,
            aluno.getResponsavel() != null ? aluno.getResponsavel().getCpf() : null,
            aluno.getResponsavel() != null ? aluno.getResponsavel().getTelefone() : null,
            aluno.getResponsavel() != null ? aluno.getResponsavel().getEmail() : null,
            aluno.getTurmas() != null ? aluno.getTurmas().stream().map(t -> t.getId()).collect(Collectors.toList()) : Collections.emptyList(),
            aluno.getTurmas() != null ? aluno.getTurmas().stream().map(t -> t.getNomeTurma()).collect(Collectors.toList()) : Collections.emptyList(),
            aluno.getIdiomas() != null ? aluno.getIdiomas() : Collections.emptyList()
        );
    }

    @GetMapping
    public ResponseEntity<List<AlunoDTO>> listarTodosAlunos() {
        List<AlunoDTO> alunos = alunoService.listarTodosAlunos().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(alunos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoDTO> buscarAlunoPorId(@PathVariable Long id) {
        return alunoService.buscarAlunoPorId(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AlunoDTO> criarAluno(@Valid @RequestBody AlunoDTO novoAluno) {
        try {
            Aluno aluno = alunoService.criarAluno(novoAluno);
            if (aluno == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(aluno));
        } catch (Exception e) {
            logger.error("Erro ao criar aluno via API: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlunoDTO> atualizarAluno(@PathVariable Long id, @Valid @RequestBody AlunoDTO alunoAtualizado) {
        try {
            Aluno aluno = alunoService.atualizarAluno(id, alunoAtualizado);
            if (aluno == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.ok(convertToDTO(aluno));
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar aluno com ID {} via API: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
        catch (Exception e) {
            logger.error("Erro geral ao atualizar aluno com ID {} via API: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAluno(@PathVariable Long id) {
        try {
            alunoService.deletarAluno(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar aluno com ID {} via API: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}