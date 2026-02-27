package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.DeclaracaoMatriculaDTO;
import br.com.arirang.plataforma.dto.FichaMatriculaDTO;
import br.com.arirang.plataforma.service.DocumentoService;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.TurmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/documentos")
public class DocumentoController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoController.class);

    @Autowired
    private DocumentoService documentoService;
    
    @Autowired
    private AlunoService alunoService;
    
    @Autowired
    private TurmaService turmaService;

    @GetMapping("/declaracao-matricula")
    public String gerarDeclaracaoMatricula(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long turmaId,
            Model model) {
        try {
            // Se não foram fornecidos parâmetros, mostrar formulário de seleção
            if (alunoId == null || turmaId == null) {
                // Carregar alunos e turmas para o formulário
                model.addAttribute("alunos", alunoService.listarTodosAlunos());
                model.addAttribute("turmas", turmaService.listarTodasTurmas());
                return "documentos/declaracao-matricula-form";
            }
            
            // Gerar declaração se parâmetros foram fornecidos
            DeclaracaoMatriculaDTO declaracao = documentoService.gerarDeclaracaoMatricula(alunoId, turmaId);
            model.addAttribute("declaracao", declaracao);
            return "declaracao-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar declaração de matrícula: ", e);
            model.addAttribute("error", "Erro ao gerar declaração: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/declaracao-matricula/aluno/{alunoId}/turma/{turmaId}")
    public String gerarDeclaracaoMatriculaPorAlunoETurma(
            @PathVariable Long alunoId,
            @PathVariable Long turmaId,
            Model model) {
        try {
            DeclaracaoMatriculaDTO declaracao = documentoService.gerarDeclaracaoMatricula(alunoId, turmaId);
            model.addAttribute("declaracao", declaracao);
            return "declaracao-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar declaração de matrícula: ", e);
            model.addAttribute("error", "Erro ao gerar declaração: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/declaracao-matricula/turma/{turmaId}")
    public String gerarDeclaracoesPorTurma(@PathVariable Long turmaId, Model model) {
        try {
            List<DeclaracaoMatriculaDTO> declaracoes = documentoService.gerarDeclaracoesPorTurma(turmaId);
            model.addAttribute("declaracoes", declaracoes);
            model.addAttribute("turmaId", turmaId);
            return "declaracoes-turma";
        } catch (Exception e) {
            logger.error("Erro ao gerar declarações por turma: ", e);
            model.addAttribute("error", "Erro ao gerar declarações: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/debug")
    public String debugContratos(Model model) {
        try {
            return "Endpoint de debug funcionando!";
        } catch (Exception e) {
            return "Erro no debug: " + e.getMessage();
        }
    }

    @GetMapping("/ficha-matricula")
    public String gerarFichaMatricula(@RequestParam Long alunoId, Model model) {
        try {
            FichaMatriculaDTO ficha = documentoService.gerarFichaMatricula(alunoId);
            model.addAttribute("ficha", ficha);
            return "ficha-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar ficha de matrícula: ", e);
            model.addAttribute("error", "Erro ao gerar ficha: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/ficha-matricula/aluno/{alunoId}")
    public String gerarFichaMatriculaPorAluno(@PathVariable Long alunoId, Model model) {
        try {
            FichaMatriculaDTO ficha = documentoService.gerarFichaMatricula(alunoId);
            model.addAttribute("ficha", ficha);
            return "ficha-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar ficha de matrícula: ", e);
            model.addAttribute("error", "Erro ao gerar ficha: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/menu")
    public String menuDocumentos(Model model) {
        return "documentos-menu";
    }

    @GetMapping("/visualizar/declaracao")
    public String visualizarDeclaracao(
            @RequestParam Long alunoId,
            @RequestParam Long turmaId,
            Model model) {
        try {
            DeclaracaoMatriculaDTO declaracao = documentoService.gerarDeclaracaoMatricula(alunoId, turmaId);
            model.addAttribute("declaracao", declaracao);
            return "declaracao-matricula-visualizar";
        } catch (Exception e) {
            logger.error("Erro ao visualizar declaração: ", e);
            model.addAttribute("error", "Erro ao visualizar declaração: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/visualizar/ficha")
    public String visualizarFicha(@RequestParam Long alunoId, Model model) {
        try {
            FichaMatriculaDTO ficha = documentoService.gerarFichaMatricula(alunoId);
            model.addAttribute("ficha", ficha);
            return "ficha-matricula-visualizar";
        } catch (Exception e) {
            logger.error("Erro ao visualizar ficha: ", e);
            model.addAttribute("error", "Erro ao visualizar ficha: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/imprimir/declaracao")
    public String imprimirDeclaracao(
            @RequestParam Long alunoId,
            @RequestParam Long turmaId,
            Model model) {
        try {
            DeclaracaoMatriculaDTO declaracao = documentoService.gerarDeclaracaoMatricula(alunoId, turmaId);
            model.addAttribute("declaracao", declaracao);
            model.addAttribute("imprimir", true);
            return "declaracao-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao imprimir declaração: ", e);
            model.addAttribute("error", "Erro ao imprimir declaração: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/imprimir/ficha")
    public String imprimirFicha(@RequestParam Long alunoId, Model model) {
        try {
            FichaMatriculaDTO ficha = documentoService.gerarFichaMatricula(alunoId);
            model.addAttribute("ficha", ficha);
            model.addAttribute("imprimir", true);
            return "ficha-matricula-pdf";
        } catch (Exception e) {
            logger.error("Erro ao imprimir ficha: ", e);
            model.addAttribute("error", "Erro ao imprimir ficha: " + e.getMessage());
            return "error";
        }
    }
}
