package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.CarneDTO;
import br.com.arirang.plataforma.service.CarneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/carne")
public class CarneController {

    private static final Logger logger = LoggerFactory.getLogger(CarneController.class);

    @Autowired
    private CarneService carneService;

    @GetMapping("/contrato/{contratoId}")
    public String gerarCarnePorContrato(@PathVariable Long contratoId, Model model) {
        try {
            CarneDTO carne = carneService.gerarCarnePorContrato(contratoId);
            model.addAttribute("carne", carne);
            return "carne-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnê por contrato: ", e);
            model.addAttribute("error", "Erro ao gerar carnê: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/aluno/{alunoId}")
    public String gerarCarnePorAluno(@PathVariable Long alunoId, Model model) {
        try {
            CarneDTO carne = carneService.gerarCarnePorAluno(alunoId);
            model.addAttribute("carne", carne);
            return "carne-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnê por aluno: ", e);
            model.addAttribute("error", "Erro ao gerar carnê: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/turma/{turmaId}")
    public String gerarCarnesPorTurma(@PathVariable Long turmaId, Model model) {
        try {
            List<CarneDTO> carnes = carneService.gerarCarnesPorTurma(turmaId);
            model.addAttribute("carnes", carnes);
            model.addAttribute("turmaId", turmaId);
            return "carnes-turma";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnês por turma: ", e);
            model.addAttribute("error", "Erro ao gerar carnês: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/vencidos")
    public String gerarCarnesVencidos(Model model) {
        try {
            List<CarneDTO> carnes = carneService.gerarCarnesVencidos();
            model.addAttribute("carnes", carnes);
            return "carnes-vencidos";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnês vencidos: ", e);
            model.addAttribute("error", "Erro ao gerar carnês: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/periodo")
    public String gerarCarnesPorPeriodo(
            @RequestParam(value = "dataInicio", required = false) String dataInicio,
            @RequestParam(value = "dataFim", required = false) String dataFim,
            Model model) {
        try {
            LocalDate inicio = dataInicio != null ? LocalDate.parse(dataInicio) : LocalDate.now().minusDays(30);
            LocalDate fim = dataFim != null ? LocalDate.parse(dataFim) : LocalDate.now();
            
            List<CarneDTO> carnes = carneService.gerarCarnesPorPeriodo(inicio, fim);
            model.addAttribute("carnes", carnes);
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);
            return "carnes-periodo";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnês por período: ", e);
            model.addAttribute("error", "Erro ao gerar carnês: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/especifico")
    public String gerarCarneEspecifico(
            @RequestParam Long contratoId,
            @RequestParam List<Long> parcelasIds,
            Model model) {
        try {
            CarneDTO carne = carneService.gerarCarneEspecifico(contratoId, parcelasIds);
            model.addAttribute("carne", carne);
            return "carne-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar carnê específico: ", e);
            model.addAttribute("error", "Erro ao gerar carnê: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/visualizar/{contratoId}")
    public String visualizarCarne(@PathVariable Long contratoId, Model model) {
        try {
            CarneDTO carne = carneService.gerarCarnePorContrato(contratoId);
            model.addAttribute("carne", carne);
            return "carne-visualizar";
        } catch (Exception e) {
            logger.error("Erro ao visualizar carnê: ", e);
            model.addAttribute("error", "Erro ao visualizar carnê: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/imprimir/{contratoId}")
    public String imprimirCarne(@PathVariable Long contratoId, Model model) {
        try {
            CarneDTO carne = carneService.gerarCarnePorContrato(contratoId);
            model.addAttribute("carne", carne);
            model.addAttribute("imprimir", true);
            return "carne-pdf";
        } catch (Exception e) {
            logger.error("Erro ao imprimir carnê: ", e);
            model.addAttribute("error", "Erro ao imprimir carnê: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/menu")
    public String menuCarne(Model model) {
        return "carne-menu";
    }
}
