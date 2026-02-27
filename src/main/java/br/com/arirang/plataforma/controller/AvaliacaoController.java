package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AvaliacaoDTO;
import br.com.arirang.plataforma.dto.NotaAvaliacaoDTO;
import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.service.AvaliacaoService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.entity.TipoNota;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoController.class);

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private TurmaService turmaService;

    @GetMapping
    public String avaliacoesMenu(Model model) {
        logger.info("Acessando menu de avaliações");
        model.addAttribute("totalAvaliacoes", avaliacaoService.contarTotalAvaliacoes());
        model.addAttribute("avaliacoesAtivas", avaliacaoService.contarAvaliacoesAtivas());
        model.addAttribute("avaliacoesFinalizadas", avaliacaoService.contarAvaliacoesFinalizadas());
        return "avaliacoes/avaliacoes-menu";
    }

    @GetMapping("/lista")
    public String listarAvaliacoes(Model model,
                                   @RequestParam(value = "nome", required = false) String nome,
                                   @RequestParam(value = "turmaId", required = false) Long turmaId,
                                   @RequestParam(value = "ativa", required = false) Boolean ativa) {
        logger.info("Listando avaliações com filtros: nome={}, turmaId={}, ativa={}", nome, turmaId, ativa);
        List<AvaliacaoDTO> avaliacoes;

        if (turmaId != null) {
            avaliacoes = avaliacaoService.listarAvaliacoesPorTurma(turmaId);
        } else {
            avaliacoes = avaliacaoService.listarTodasAvaliacoes();
        }

        if (ativa != null) {
            avaliacoes = avaliacoes.stream()
                    .filter(a -> a.ativa() == ativa)
                    .collect(Collectors.toList());
        }

        List<Turma> turmas = turmaService.listarTodasTurmas();
        List<TurmaDTO> turmasDTO = turmas.stream()
            .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
            .collect(Collectors.toList());

        model.addAttribute("avaliacoes", avaliacoes);
        model.addAttribute("turmas", turmasDTO);
        model.addAttribute("totalAvaliacoes", avaliacoes.size());

        return "avaliacoes/avaliacoes-lista";
    }

    @GetMapping("/nova")
    public String novaAvaliacaoForm(Model model) {
        logger.info("Carregando formulário de nova avaliação");

        List<Turma> turmas = turmaService.listarTodasTurmas();
        List<TurmaDTO> turmasDTO = turmas.stream()
            .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
            .collect(Collectors.toList());

        model.addAttribute("avaliacaoDTO", new AvaliacaoDTO(null, null, null, null, null, null, LocalDate.now(), null, null, 1, 100, true, null));
        model.addAttribute("turmas", turmasDTO);
        model.addAttribute("tiposAvaliacao", TipoNota.values());

        return "avaliacoes/avaliacoes-nova";
    }

    @PostMapping("/nova")
    public String criarAvaliacao(@Valid @ModelAttribute AvaliacaoDTO avaliacaoDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        logger.info("Recebida requisição para criar avaliação: {}", avaliacaoDTO.nomeAvaliacao());

        if (result.hasErrors()) {
            logger.warn("Erros de validação ao criar avaliação: {}", result.getAllErrors());
            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(Collectors.toList());
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("tiposAvaliacao", TipoNota.values());
            return "avaliacoes/avaliacoes-nova";
        }

        try {
            avaliacaoService.criarAvaliacao(avaliacaoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Avaliação criada com sucesso!");
            return "redirect:/avaliacoes/lista";
        } catch (Exception e) {
            logger.error("Erro ao criar avaliação: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar avaliação: " + e.getMessage());
            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(Collectors.toList());
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("tiposAvaliacao", TipoNota.values());
            return "avaliacoes/avaliacoes-nova";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarAvaliacaoForm(@PathVariable Long id, Model model) {
        logger.info("Carregando formulário de edição da avaliação: {}", id);

        try {
            AvaliacaoDTO avaliacao = avaliacaoService.buscarAvaliacaoPorId(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(Collectors.toList());

            model.addAttribute("avaliacaoDTO", avaliacao);
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("tiposAvaliacao", TipoNota.values());

            return "avaliacoes/avaliacoes-editar";

        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição da avaliação {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Erro ao carregar avaliação: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/editar/{id}")
    public String atualizarAvaliacao(@PathVariable Long id,
                                     @Valid @ModelAttribute AvaliacaoDTO avaliacaoDTO,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        logger.info("Recebida requisição para atualizar avaliação ID: {}", id);

        if (result.hasErrors()) {
            logger.warn("Erros de validação ao atualizar avaliação ID {}: {}", id, result.getAllErrors());
            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(Collectors.toList());
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("tiposAvaliacao", TipoNota.values());
            return "avaliacoes/avaliacoes-editar";
        }

        try {
            avaliacaoService.atualizarAvaliacao(id, avaliacaoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Avaliação atualizada com sucesso!");
            return "redirect:/avaliacoes/lista";
        } catch (Exception e) {
            logger.error("Erro ao atualizar avaliação ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar avaliação: " + e.getMessage());
            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(Collectors.toList());
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("tiposAvaliacao", TipoNota.values());
            return "avaliacoes/avaliacoes-editar";
        }
    }

    @PostMapping("/deletar/{id}")
    public String deletarAvaliacao(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Recebida requisição para deletar avaliação ID: {}", id);
        try {
            avaliacaoService.deletarAvaliacao(id);
            redirectAttributes.addFlashAttribute("successMessage", "Avaliação deletada com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao deletar avaliação ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar avaliação: " + e.getMessage());
        }
        return "redirect:/avaliacoes/lista";
    }

    @GetMapping("/detalhes/{id}")
    public String detalhesAvaliacao(@PathVariable Long id, Model model) {
        logger.info("Carregando detalhes da avaliação: {}", id);
        try {
            AvaliacaoDTO avaliacao = avaliacaoService.buscarAvaliacaoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
            
            List<NotaAvaliacaoDTO> notas = avaliacaoService.listarNotasPorAvaliacao(id);
            
            model.addAttribute("avaliacao", avaliacao);
            model.addAttribute("notas", notas);
            return "avaliacoes/avaliacoes-detalhes";
        } catch (Exception e) {
            logger.error("Erro ao carregar detalhes da avaliação {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Erro ao carregar detalhes da avaliação: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/lancar-notas/{avaliacaoId}")
    public String lancarNotas(@PathVariable Long avaliacaoId,
                              @ModelAttribute("notasAvaliacao") List<NotaAvaliacaoDTO> notasAvaliacao,
                              RedirectAttributes redirectAttributes) {
        logger.info("Lançando notas para avaliação ID: {}", avaliacaoId);
        try {
            for (NotaAvaliacaoDTO notaDTO : notasAvaliacao) {
                avaliacaoService.atualizarNotaAvaliacao(notaDTO.id(), notaDTO);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Notas lançadas com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao lançar notas para avaliação ID {}: {}", avaliacaoId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao lançar notas: " + e.getMessage());
        }
        return "redirect:/avaliacoes/detalhes/" + avaliacaoId;
    }
}