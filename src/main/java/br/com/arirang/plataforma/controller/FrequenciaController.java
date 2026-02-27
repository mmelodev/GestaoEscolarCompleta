package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.FrequenciaDTO;
import br.com.arirang.plataforma.dto.FrequenciaFormDTO;
import br.com.arirang.plataforma.dto.RelatorioFrequenciaDTO;
import br.com.arirang.plataforma.service.FrequenciaAlertaService;
import br.com.arirang.plataforma.service.FrequenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/frequencia")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class FrequenciaController {

    private static final Logger logger = LoggerFactory.getLogger(FrequenciaController.class);

    @Autowired
    private FrequenciaService frequenciaService;

    @Autowired
    private FrequenciaAlertaService frequenciaAlertaService;

    @Autowired
    private br.com.arirang.plataforma.service.TurmaService turmaService;

    /**
     * Página principal de frequência
     */
    @GetMapping
    public String index(Model model) {
        return "frequencia/index";
    }

    /**
     * Formulário para registrar frequência de uma turma em uma data
     */
    @GetMapping("/registrar")
    public String formRegistrar(
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAula,
            Model model) {
        
        if (dataAula == null) {
            dataAula = LocalDate.now();
        }
        
        // Listar todas as turmas para seleção
        List<br.com.arirang.plataforma.entity.Turma> turmas = turmaService.listarTodasTurmas();
        model.addAttribute("turmas", turmas);
        model.addAttribute("dataAula", dataAula);
        model.addAttribute("turmaId", turmaId);
        
        if (turmaId != null) {
            List<FrequenciaDTO> frequencias = frequenciaService.listarPorTurmaEData(turmaId, dataAula);
            model.addAttribute("frequencias", frequencias);
            
            // Buscar alunos da turma
            java.util.Optional<br.com.arirang.plataforma.entity.Turma> turmaOpt = 
                turmaService.buscarTurmaPorId(turmaId);
            if (turmaOpt.isPresent()) {
                model.addAttribute("alunos", turmaOpt.get().getAlunos());
            }
        }
        
        return "frequencia/registrar";
    }

    /**
     * Salva frequência individual
     */
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute FrequenciaFormDTO formDTO,
                        RedirectAttributes redirectAttributes) {
        try {
            frequenciaService.registrarFrequencia(formDTO);
            redirectAttributes.addFlashAttribute("success", "Frequência registrada com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao salvar frequência: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar frequência: " + e.getMessage());
        }
        return "redirect:/frequencia/registrar?turmaId=" + formDTO.turmaId() + 
               "&dataAula=" + formDTO.dataAula();
    }

    /**
     * Salva frequência em lote
     */
    @PostMapping("/salvar-lote")
    public String salvarLote(
            @RequestParam Long turmaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAula,
            @RequestParam List<Long> alunoIds,
            @RequestParam List<String> tiposPresenca,
            @RequestParam(required = false) List<String> observacoes,
            RedirectAttributes redirectAttributes) {
        try {
            List<FrequenciaFormDTO> frequencias = new java.util.ArrayList<>();
            for (int i = 0; i < alunoIds.size(); i++) {
                Long alunoId = alunoIds.get(i);
                String tipoPresencaStr = tiposPresenca.get(i);
                String observacao = (observacoes != null && i < observacoes.size()) ? observacoes.get(i) : null;
                
                br.com.arirang.plataforma.enums.TipoPresenca tipoPresenca = 
                    br.com.arirang.plataforma.enums.TipoPresenca.valueOf(tipoPresencaStr);
                
                FrequenciaFormDTO formDTO = new FrequenciaFormDTO(
                    alunoId, turmaId, dataAula, tipoPresenca, observacao, null
                );
                frequencias.add(formDTO);
            }
            
            frequenciaService.registrarFrequenciaLote(turmaId, dataAula, frequencias);
            redirectAttributes.addFlashAttribute("success", "Frequências registradas com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao salvar frequências em lote: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar frequências: " + e.getMessage());
        }
        return "redirect:/frequencia/registrar?turmaId=" + turmaId + "&dataAula=" + dataAula;
    }

    /**
     * Visualiza frequência de um aluno
     */
    @GetMapping("/aluno/{alunoId}/turma/{turmaId}")
    public String visualizarAluno(
            @PathVariable Long alunoId,
            @PathVariable Long turmaId,
            Model model) {
        List<FrequenciaDTO> frequencias = frequenciaService.listarPorAlunoETurma(alunoId, turmaId);
        model.addAttribute("frequencias", frequencias);
        model.addAttribute("alunoId", alunoId);
        model.addAttribute("turmaId", turmaId);
        return "frequencia/aluno";
    }

    /**
     * Relatório de frequência de um aluno
     */
    @GetMapping("/relatorio/aluno/{alunoId}/turma/{turmaId}")
    public String relatorioAluno(
            @PathVariable Long alunoId,
            @PathVariable Long turmaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model) {
        
        if (dataInicio == null) {
            dataInicio = LocalDate.now().minusMonths(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }

        RelatorioFrequenciaDTO relatorio = frequenciaService.gerarRelatorioAluno(
                alunoId, turmaId, dataInicio, dataFim);
        
        model.addAttribute("relatorio", relatorio);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        
        return "frequencia/relatorio-aluno";
    }

    /**
     * Relatório de frequência de uma turma
     */
    @GetMapping("/relatorio/turma/{turmaId}")
    public String relatorioTurma(
            @PathVariable Long turmaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Double percentualMinimo,
            Model model) {
        
        if (dataInicio == null) {
            dataInicio = LocalDate.now().minusMonths(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }
        if (percentualMinimo == null) {
            percentualMinimo = 75.0;
        }

        List<RelatorioFrequenciaDTO> relatorios = frequenciaService.listarAlunosComFrequenciaBaixa(
                turmaId, dataInicio, dataFim, percentualMinimo);
        
        model.addAttribute("relatorios", relatorios);
        model.addAttribute("turmaId", turmaId);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("percentualMinimo", percentualMinimo);
        
        return "frequencia/relatorio-turma";
    }

    /**
     * Alertas de frequência baixa
     */
    @GetMapping("/alertas")
    public String alertas(
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Double percentualMinimo,
            Model model) {
        
        if (dataInicio == null) {
            dataInicio = LocalDate.now().minusMonths(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }
        if (percentualMinimo == null) {
            percentualMinimo = 75.0;
        }

        // Listar todas as turmas para o filtro
        List<br.com.arirang.plataforma.entity.Turma> turmas = turmaService.listarTodasTurmas();
        model.addAttribute("turmas", turmas);

        List<RelatorioFrequenciaDTO> alertas;
        if (turmaId != null) {
            alertas = frequenciaAlertaService.gerarAlertasTurma(
                    turmaId, dataInicio, dataFim, percentualMinimo);
        } else {
            // Listar alertas de todas as turmas
            alertas = new java.util.ArrayList<>();
            for (br.com.arirang.plataforma.entity.Turma turma : turmas) {
                List<RelatorioFrequenciaDTO> alertasTurma = frequenciaAlertaService.gerarAlertasTurma(
                        turma.getId(), dataInicio, dataFim, percentualMinimo);
                alertas.addAll(alertasTurma);
            }
        }
        
        model.addAttribute("alertas", alertas);
        model.addAttribute("turmaId", turmaId);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("percentualMinimo", percentualMinimo);
        
        return "frequencia/alertas";
    }

    /**
     * Remove frequência
     */
    @PostMapping("/remover/{id}")
    public String remover(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            frequenciaService.removerFrequencia(id);
            redirectAttributes.addFlashAttribute("success", "Frequência removida com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao remover frequência: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao remover frequência: " + e.getMessage());
        }
        return "redirect:/frequencia";
    }
}
