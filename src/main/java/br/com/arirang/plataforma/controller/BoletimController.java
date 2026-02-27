package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.BoletimDTO;
import br.com.arirang.plataforma.dto.NotaDTO;
import br.com.arirang.plataforma.dto.AlunoTurmaDTO;
import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.service.BoletimService;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/boletim")
public class BoletimController {

    private static final Logger logger = LoggerFactory.getLogger(BoletimController.class);

    @Autowired
    private BoletimService boletimService;
    
    @Autowired
    private AlunoService alunoService;
    
    @Autowired
    private TurmaService turmaService;
    
    @Autowired
    private AvaliacaoService avaliacaoService;

    @GetMapping
    public String listarBoletins(Model model) {
        try {
            // Carregar estatísticas reais de boletins
            Long totalBoletins = boletimService.contarTotalBoletins();
            Long boletinsPendentes = boletimService.contarBoletinsPendentes();
            Long boletinsFinalizados = boletimService.contarBoletinsFinalizados();
            
            model.addAttribute("totalBoletins", totalBoletins != null ? totalBoletins : 0);
            model.addAttribute("boletinsPendentes", boletinsPendentes != null ? boletinsPendentes : 0);
            model.addAttribute("boletinsFinalizados", boletinsFinalizados != null ? boletinsFinalizados : 0);
            
            // Carregar alunos e turmas para os filtros usando DTOs
            List<Aluno> alunos = alunoService.listarTodosAlunos();
            List<Turma> turmas = turmaService.listarTodasTurmas();
            
            // Converter entidades para DTOs para evitar serialização circular
            List<AlunoTurmaDTO> alunosDTO = alunos.stream()
                .map(aluno -> {
                    List<TurmaDTO> turmasDTO = aluno.getTurmas() != null ? 
                        aluno.getTurmas().stream()
                            .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                            .toList() : List.of();
                    
                    return new AlunoTurmaDTO(aluno.getId(), aluno.getNomeCompleto(), turmasDTO);
                })
                .toList();
            
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .toList();
            
            model.addAttribute("alunos", alunosDTO);
            model.addAttribute("turmas", turmasDTO);
            
            return "boletim-menu";
        } catch (Exception e) {
            logger.error("Erro ao carregar menu de boletins: ", e);
            model.addAttribute("error", "Erro ao carregar boletins: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/buscar")
    public String buscarBoletim(@RequestParam(value = "alunoId", required = false) Long alunoId,
                               @RequestParam(value = "turmaId", required = false) Long turmaId,
                               Model model) {
        try {
            if (alunoId != null && turmaId != null) {
                // Buscar boletim específico do aluno na turma
                return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId;
            } else if (alunoId != null) {
                // Buscar todos os boletins do aluno
                List<BoletimDTO> boletinsDTO = boletimService.buscarBoletinsPorAlunoAsDTO(alunoId);
                model.addAttribute("alunoId", alunoId);
                model.addAttribute("boletins", boletinsDTO);
                return "boletim-lista-aluno";
            } else if (turmaId != null) {
                // Buscar todos os boletins da turma
                List<BoletimDTO> boletinsDTO = boletimService.buscarBoletinsPorTurmaAsDTO(turmaId);
                model.addAttribute("turmaId", turmaId);
                model.addAttribute("boletins", boletinsDTO);
                return "boletim-lista-turma";
            } else {
                // Nenhum filtro selecionado, voltar ao menu
                return "redirect:/boletim";
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar boletim: ", e);
            model.addAttribute("error", "Erro ao buscar boletim: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/aluno/{alunoId}/turma/{turmaId}")
    public String boletimForm(@PathVariable Long alunoId, @PathVariable Long turmaId, Model model) {
        try {
            // Usar método que retorna DTO diretamente, garantindo que tudo seja feito dentro da transação
            BoletimDTO boletimDTO = boletimService.buscarBoletimPorAlunoETurmaAsDTO(alunoId, turmaId)
                    .orElseGet(() -> boletimService.criarBoletimAsDTO(alunoId, turmaId));
            
            model.addAttribute("boletim", boletimDTO);
            model.addAttribute("alunoId", alunoId);
            model.addAttribute("turmaId", turmaId);
            
            return "boletim-form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de boletim: ", e);
            model.addAttribute("error", "Erro ao carregar boletim: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/adicionar-nota")
    public String adicionarNota(@Valid @ModelAttribute("nota") NotaDTO notaDTO, 
                                @RequestParam("boletimId") Long boletimId,
                                @RequestParam("alunoId") Long alunoId,
                                @RequestParam("turmaId") Long turmaId,
                                BindingResult bindingResult, Model model) {
        try {
            if (bindingResult.hasErrors()) {
                BoletimDTO boletimDTO = boletimService.buscarBoletimPorIdAsDTO(boletimId)
                        .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));
                model.addAttribute("boletim", boletimDTO);
                model.addAttribute("alunoId", alunoId);
                model.addAttribute("turmaId", turmaId);
                return "boletim-form";
            }

            boletimService.adicionarNota(boletimId, notaDTO);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId;
        } catch (Exception e) {
            logger.error("Erro ao adicionar nota: ", e);
            model.addAttribute("error", "Erro ao adicionar nota: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/remover-nota/{notaId}")
    public String removerNota(@PathVariable Long notaId,
                             @RequestParam("alunoId") Long alunoId,
                             @RequestParam("turmaId") Long turmaId) {
        try {
            boletimService.removerNota(notaId);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId;
        } catch (Exception e) {
            logger.error("Erro ao remover nota: ", e);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/salvar/{boletimId}")
    public String salvarBoletim(@PathVariable Long boletimId,
                                @RequestParam("alunoId") Long alunoId,
                                @RequestParam("turmaId") Long turmaId,
                                Model model) {
        try {
            boletimService.salvarBoletim(boletimId);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?success=Boletim salvo com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao salvar boletim: ", e);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/finalizar/{boletimId}")
    public String finalizarBoletim(@PathVariable Long boletimId,
                                  @RequestParam("alunoId") Long alunoId,
                                  @RequestParam("turmaId") Long turmaId,
                                  Model model) {
        try {
            boletimService.finalizarBoletim(boletimId);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?success=Boletim finalizado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao finalizar boletim: ", e);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/atualizar-media/{boletimId}")
    public String atualizarMediaFinal(@PathVariable Long boletimId,
                                     @RequestParam("mediaFinal") Double mediaFinal,
                                     @RequestParam("alunoId") Long alunoId,
                                     @RequestParam("turmaId") Long turmaId,
                                     Model model) {
        try {
            if (mediaFinal == null || mediaFinal < 0 || mediaFinal > 10) {
                return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?error=Média inválida. Deve estar entre 0 e 10.";
            }
            boletimService.atualizarMediaFinal(boletimId, mediaFinal);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?success=Média atualizada com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao atualizar média do boletim: ", e);
            return "redirect:/boletim/aluno/" + alunoId + "/turma/" + turmaId + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/pdf/{boletimId}")
    public String gerarPdfBoletim(@PathVariable Long boletimId, Model model) {
        try {
            BoletimDTO boletimDTO = boletimService.buscarBoletimPorIdAsDTO(boletimId)
                    .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));
            
            if (!boletimDTO.finalizado()) {
                throw new RuntimeException("Boletim deve estar finalizado para gerar PDF");
            }
            
            model.addAttribute("boletim", boletimDTO);
            
            return "boletim-pdf";
        } catch (Exception e) {
            logger.error("Erro ao gerar PDF do boletim: ", e);
            model.addAttribute("error", "Erro ao gerar PDF: " + e.getMessage());
            return "error";
        }
    }

    @Autowired
    private br.com.arirang.plataforma.service.BoletimPdfService boletimPdfService;

    @GetMapping("/download-pdf/{boletimId}")
    public void downloadPdfBoletim(@PathVariable Long boletimId, 
                                   jakarta.servlet.http.HttpServletResponse response) {
        try {
            BoletimDTO boletimDTO = boletimService.buscarBoletimPorIdAsDTO(boletimId)
                    .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));
            
            if (!boletimDTO.finalizado()) {
                throw new RuntimeException("Boletim deve estar finalizado para baixar PDF");
            }
            
            // Gerar PDF
            byte[] pdfBytes = boletimPdfService.gerarPdf(boletimDTO);
            
            // Configurar headers para download
            String fileName = "boletim_" + boletimDTO.alunoNome().replaceAll("[^a-zA-Z0-9]", "_") + "_" + boletimId + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLength(pdfBytes.length);
            
            // Escrever PDF na resposta
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            logger.error("Erro ao baixar PDF do boletim: ", e);
            try {
                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Erro ao gerar PDF: " + e.getMessage());
            } catch (IOException ioException) {
                logger.error("Erro ao enviar resposta de erro: ", ioException);
            }
        }
    }

    @GetMapping("/relatorio")
    public String relatorioBoletins(Model model) {
        try {
            // Carregar dados reais de boletins
            Long totalBoletins = boletimService.contarTotalBoletins();
            Long boletinsPendentes = boletimService.contarBoletinsPendentes();
            Long boletinsFinalizados = boletimService.contarBoletinsFinalizados();
            Double mediaGeral = boletimService.calcularMediaGeral();
            
            model.addAttribute("totalBoletins", totalBoletins != null ? totalBoletins : 0);
            model.addAttribute("boletinsPendentes", boletinsPendentes != null ? boletinsPendentes : 0);
            model.addAttribute("boletinsFinalizados", boletinsFinalizados != null ? boletinsFinalizados : 0);
            model.addAttribute("mediaGeral", mediaGeral != null ? mediaGeral : 0.0);
            
            // Dados para gráfico
            Long aprovados = boletimService.contarAprovados();
            Long reprovados = boletimService.contarReprovados();
            model.addAttribute("aprovados", aprovados != null ? aprovados : 0);
            model.addAttribute("reprovados", reprovados != null ? reprovados : 0);
            
            return "boletim-relatorio";
        } catch (Exception e) {
            logger.error("Erro ao carregar relatórios de boletins: ", e);
            model.addAttribute("error", "Erro ao carregar relatórios: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/relatorio/turma")
    public String relatorioPorTurma(
            @RequestParam(value = "turmaId", required = false) Long turmaId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "periodo", required = false) String periodo,
            Model model) {
        try {
            // Carregar todas as turmas COM alunos (usando JOIN FETCH)
            List<Turma> todasTurmas = turmaService.listarTodasTurmasComAlunos();
            
            // Criar DTOs com estatísticas
            List<TurmaRelatorioDTO> turmasRelatorio = todasTurmas.stream()
                    .map(turma -> {
                        // Agora podemos acessar alunos com segurança pois foram carregados com JOIN FETCH
                        Long totalAlunos = turma.getAlunos() != null ? (long) turma.getAlunos().size() : 0L;
                        Double mediaGeral = boletimService.calcularMediaPorTurma(turma.getId());
                        Long aprovados = boletimService.contarAprovadosPorTurma(turma.getId());
                        Long reprovados = boletimService.contarReprovadosPorTurma(turma.getId());
                        Double taxaAprovacao = boletimService.calcularTaxaAprovacaoPorTurma(turma.getId());
                        
                        return new TurmaRelatorioDTO(
                                turma.getId(),
                                turma.getNomeTurma(),
                                turma.getNivelProficiencia(),
                                totalAlunos,
                                mediaGeral != null ? mediaGeral : 0.0,
                                taxaAprovacao != null ? taxaAprovacao : 0.0,
                                aprovados != null ? aprovados : 0L,
                                reprovados != null ? reprovados : 0L,
                                turma.getSituacaoTurma() != null ? turma.getSituacaoTurma() : "ABERTA"
                        );
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            // Aplicar filtros
            if (turmaId != null) {
                turmasRelatorio = turmasRelatorio.stream()
                        .filter(t -> t.id().equals(turmaId))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            if (status != null && !status.isEmpty()) {
                turmasRelatorio = turmasRelatorio.stream()
                        .filter(t -> status.equalsIgnoreCase(t.statusTurma()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Filtro de período (baseado em anoSemestre da turma)
            if (periodo != null && !periodo.isEmpty()) {
                List<Turma> turmasFiltradasPorPeriodo = todasTurmas.stream()
                        .filter(t -> t.getAnoSemestre() != null && t.getAnoSemestre().contains(periodo))
                        .collect(java.util.stream.Collectors.toList());
                
                List<Long> idsTurmasPeriodo = turmasFiltradasPorPeriodo.stream()
                        .map(Turma::getId)
                        .collect(java.util.stream.Collectors.toList());
                
                turmasRelatorio = turmasRelatorio.stream()
                        .filter(t -> idsTurmasPeriodo.contains(t.id()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Carregar todas as turmas para o select de filtro
            List<TurmaDTO> turmasParaFiltro = todasTurmas.stream()
                    .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                    .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("turmas", turmasRelatorio);
            model.addAttribute("turmasParaFiltro", turmasParaFiltro);
            model.addAttribute("turmaIdSelecionado", turmaId);
            model.addAttribute("statusSelecionado", status);
            model.addAttribute("periodoSelecionado", periodo);
            
            // Estatísticas gerais (após filtros)
            model.addAttribute("totalTurmas", turmasRelatorio.size());
            model.addAttribute("totalAlunos", turmasRelatorio.stream()
                    .mapToLong(TurmaRelatorioDTO::totalAlunos)
                    .sum());
            model.addAttribute("mediaGeral", turmasRelatorio.stream()
                    .filter(t -> t.mediaGeral() > 0)
                    .mapToDouble(TurmaRelatorioDTO::mediaGeral)
                    .average()
                    .orElse(0.0));
            model.addAttribute("taxaAprovacaoGeral", turmasRelatorio.stream()
                    .filter(t -> t.taxaAprovacao() > 0)
                    .mapToDouble(TurmaRelatorioDTO::taxaAprovacao)
                    .average()
                    .orElse(0.0));
            
            return "boletim-relatorio-turma";
        } catch (Exception e) {
            logger.error("Erro ao carregar relatório por turma: ", e);
            model.addAttribute("error", "Erro ao carregar relatório: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/relatorio/turma/{turmaId}/detalhes")
    public String detalhesTurma(@PathVariable Long turmaId, Model model) {
        try {
            // Buscar turma com alunos
            Turma turma = turmaService.listarTodasTurmasComAlunos().stream()
                    .filter(t -> t.getId().equals(turmaId))
                    .findFirst()
                    .orElse(null);
            
            if (turma == null) {
                model.addAttribute("error", "Turma não encontrada");
                return "error";
            }
            
            // Buscar boletins da turma
            List<BoletimDTO> boletins = boletimService.buscarBoletinsPorTurmaAsDTO(turmaId);
            
            // Estatísticas da turma
            Double mediaGeral = boletimService.calcularMediaPorTurma(turmaId);
            Long aprovados = boletimService.contarAprovadosPorTurma(turmaId);
            Long reprovados = boletimService.contarReprovadosPorTurma(turmaId);
            Double taxaAprovacao = boletimService.calcularTaxaAprovacaoPorTurma(turmaId);
            
            model.addAttribute("turma", turma);
            model.addAttribute("boletins", boletins);
            model.addAttribute("totalAlunos", turma.getAlunos() != null ? turma.getAlunos().size() : 0);
            model.addAttribute("mediaGeral", mediaGeral != null ? mediaGeral : 0.0);
            model.addAttribute("aprovados", aprovados != null ? aprovados : 0L);
            model.addAttribute("reprovados", reprovados != null ? reprovados : 0L);
            model.addAttribute("taxaAprovacao", taxaAprovacao != null ? taxaAprovacao : 0.0);
            
            return "boletim-relatorio-turma-detalhes";
        } catch (Exception e) {
            logger.error("Erro ao carregar detalhes da turma: ", e);
            model.addAttribute("error", "Erro ao carregar detalhes: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/relatorio/aluno")
    public String relatorioPorAluno(
            @RequestParam(value = "turmaId", required = false) Long turmaId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "media", required = false) String media,
            Model model) {
        try {
            List<Aluno> todosAlunos = alunoService.listarTodosAlunos();
            
            // Filtrar por turma se especificado
            if (turmaId != null) {
                todosAlunos = todosAlunos.stream()
                        .filter(aluno -> aluno.getTurmas() != null && 
                                aluno.getTurmas().stream()
                                        .anyMatch(t -> t.getId().equals(turmaId)))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Criar DTOs com estatísticas
            List<AlunoRelatorioDTO> alunosRelatorio = todosAlunos.stream()
                    .map(aluno -> {
                        List<BoletimDTO> boletins = boletimService.buscarBoletinsPorAlunoAsDTO(aluno.getId());
                        Double mediaGeral = boletimService.calcularMediaPorAluno(aluno.getId());
                        String statusBoletim = "EM_ANDAMENTO";
                        if (!boletins.isEmpty()) {
                            BoletimDTO ultimoBoletim = boletins.get(0);
                            if (ultimoBoletim.finalizado() && ultimoBoletim.situacaoFinal() != null) {
                                statusBoletim = ultimoBoletim.situacaoFinal();
                            }
                        }
                        
                        // Filtrar por status se especificado
                        if (status != null && !status.isEmpty() && !statusBoletim.equals(status)) {
                            return null;
                        }
                        
                        // Filtrar por média se especificado
                        if (media != null && !media.isEmpty() && mediaGeral != null) {
                            if ("alta".equals(media) && mediaGeral < 8.0) return null;
                            if ("media".equals(media) && (mediaGeral < 6.0 || mediaGeral >= 8.0)) return null;
                            if ("baixa".equals(media) && mediaGeral >= 6.0) return null;
                        }
                        
                        String turmaNome = aluno.getTurmas() != null && !aluno.getTurmas().isEmpty() 
                                ? aluno.getTurmas().get(0).getNomeTurma() 
                                : "N/A";

                        Long turmaIdParaBoletim = aluno.getTurmas() != null && !aluno.getTurmas().isEmpty()
                                ? aluno.getTurmas().get(0).getId()
                                : null;
                        
                        return new AlunoRelatorioDTO(
                                aluno.getId(),
                                aluno.getNomeCompleto(),
                                turmaNome,
                                turmaIdParaBoletim,
                                mediaGeral != null ? mediaGeral : 0.0,
                                statusBoletim,
                                boletins.isEmpty() ? null : boletins.get(0).dataLancamento()
                        );
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("alunos", alunosRelatorio);
            
            // Carregar turmas para filtro
            List<TurmaDTO> turmasDTO = turmaService.listarTodasTurmas().stream()
                    .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("turmas", turmasDTO);
            
            // Estatísticas gerais
            model.addAttribute("totalAlunos", alunosRelatorio.size());
            model.addAttribute("aprovados", alunosRelatorio.stream()
                    .filter(a -> "APROVADO".equals(a.statusBoletim()))
                    .count());
            model.addAttribute("reprovados", alunosRelatorio.stream()
                    .filter(a -> "REPROVADO".equals(a.statusBoletim()))
                    .count());
            model.addAttribute("mediaGeral", alunosRelatorio.stream()
                    .filter(a -> a.mediaGeral() > 0)
                    .mapToDouble(AlunoRelatorioDTO::mediaGeral)
                    .average()
                    .orElse(0.0));
            
            return "boletim-relatorio-aluno";
        } catch (Exception e) {
            logger.error("Erro ao carregar relatório por aluno: ", e);
            model.addAttribute("error", "Erro ao carregar relatório: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/relatorio/geral")
    public String relatorioGeral(Model model) {
        try {
            // Estatísticas gerais
            Long totalBoletins = boletimService.contarTotalBoletins();
            Long boletinsPendentes = boletimService.contarBoletinsPendentes();
            Long boletinsFinalizados = boletimService.contarBoletinsFinalizados();
            Double mediaGeral = boletimService.calcularMediaGeral();
            Long aprovados = boletimService.contarAprovados();
            Long reprovados = boletimService.contarReprovados();
            
            model.addAttribute("totalBoletins", totalBoletins != null ? totalBoletins : 0);
            model.addAttribute("boletinsPendentes", boletinsPendentes != null ? boletinsPendentes : 0);
            model.addAttribute("boletinsFinalizados", boletinsFinalizados != null ? boletinsFinalizados : 0);
            model.addAttribute("mediaGeral", mediaGeral != null ? mediaGeral : 0.0);
            model.addAttribute("aprovados", aprovados != null ? aprovados : 0);
            model.addAttribute("reprovados", reprovados != null ? reprovados : 0);
            
            // Taxa de aprovação
            double taxaAprovacao = 0.0;
            if (boletinsFinalizados != null && boletinsFinalizados > 0 && aprovados != null) {
                taxaAprovacao = Math.round((aprovados * 100.0 / boletinsFinalizados) * 100.0) / 100.0;
            }
            model.addAttribute("taxaAprovacao", taxaAprovacao);
            
            // Estatísticas por turma (usar método que carrega alunos com JOIN FETCH)
            List<Turma> turmas = turmaService.listarTodasTurmasComAlunos();
            model.addAttribute("totalTurmas", turmas.size());
            model.addAttribute("totalAlunos", turmas.stream()
                    .mapToLong(t -> t.getAlunos() != null ? t.getAlunos().size() : 0)
                    .sum());
            
            return "boletim-relatorio-geral";
        } catch (Exception e) {
            logger.error("Erro ao carregar relatório geral: ", e);
            model.addAttribute("error", "Erro ao carregar relatório: " + e.getMessage());
            return "error";
        }
    }
    
    // DTOs internos para relatórios
    private record TurmaRelatorioDTO(
            Long id,
            String nomeTurma,
            String nivelProficiencia,
            Long totalAlunos,
            Double mediaGeral,
            Double taxaAprovacao,
            Long aprovados,
            Long reprovados,
            String statusTurma
    ) {}
    
    private record AlunoRelatorioDTO(
            Long id,
            String nomeCompleto,
            String turmaNome,
            Long turmaId,
            Double mediaGeral,
            String statusBoletim,
            java.time.LocalDateTime ultimaAtualizacao
    ) {}

    @GetMapping("/novo")
    public String novoBoletimForm(Model model) {
        try {
            // Carregar alunos com suas turmas relacionadas e turmas para seleção
            List<Aluno> alunos = alunoService.listarTodosAlunos();
            List<Turma> turmas = turmaService.listarTodasTurmas();
            
            logger.info("Carregados {} alunos e {} turmas", alunos.size(), turmas.size());
            
            // Converter entidades para DTOs para evitar serialização circular
            List<AlunoTurmaDTO> alunosDTO = alunos.stream()
                .map(aluno -> {
                    List<TurmaDTO> turmasDTO = aluno.getTurmas() != null ? 
                        aluno.getTurmas().stream()
                            .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                            .toList() : List.of();
                    
                    return new AlunoTurmaDTO(aluno.getId(), aluno.getNomeCompleto(), turmasDTO);
                })
                .toList();
            
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .toList();
            
            // Log para debug das relações aluno-turma
            for (AlunoTurmaDTO aluno : alunosDTO) {
                if (aluno.getTurmas() != null && !aluno.getTurmas().isEmpty()) {
                    logger.info("Aluno {} tem {} turmas: {}", 
                        aluno.getNomeCompleto(), 
                        aluno.getTurmas().size(),
                        aluno.getTurmas().stream().map(TurmaDTO::nomeTurma).toList());
                }
            }
            
            model.addAttribute("alunos", alunosDTO);
            model.addAttribute("turmas", turmasDTO);
            return "boletim-novo";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de novo boletim: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/novo/aluno/{alunoId}")
    public String novoBoletimParaAluno(@PathVariable Long alunoId, Model model) {
        try {
            // Carregar dados do aluno e turmas disponíveis
            model.addAttribute("aluno", alunoService.buscarAlunoPorId(alunoId).orElse(null));
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            model.addAttribute("alunoId", alunoId);
            return "boletim-novo-aluno";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário para aluno: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/novo/turma/{turmaId}")
    public String novoBoletimParaTurma(@PathVariable Long turmaId, Model model) {
        try {
            // Carregar dados da turma e alunos disponíveis
            model.addAttribute("turma", turmaService.buscarTurmaPorId(turmaId).orElse(null));
            model.addAttribute("alunos", alunoService.listarTodosAlunos());
            model.addAttribute("turmaId", turmaId);
            return "boletim-novo-turma";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário para turma: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/debug")
    @ResponseBody
    public String debugAlunosETurmas() {
        try {
            List<Aluno> alunos = alunoService.listarTodosAlunos();
            List<Turma> turmas = turmaService.listarTodasTurmas();
            
            StringBuilder debug = new StringBuilder();
            debug.append("=== DEBUG ALUNOS E TURMAS ===\n");
            debug.append("Total de alunos: ").append(alunos.size()).append("\n");
            debug.append("Total de turmas: ").append(turmas.size()).append("\n\n");
            
            debug.append("=== ALUNOS ===\n");
            for (Aluno aluno : alunos) {
                debug.append("ID: ").append(aluno.getId())
                     .append(" | Nome: ").append(aluno.getNomeCompleto())
                     .append(" | Turmas: ");
                
                if (aluno.getTurmas() != null && !aluno.getTurmas().isEmpty()) {
                    for (Turma turma : aluno.getTurmas()) {
                        debug.append(turma.getNomeTurma()).append(" (ID: ").append(turma.getId()).append(") ");
                    }
                } else {
                    debug.append("NENHUMA");
                }
                debug.append("\n");
            }
            
            debug.append("\n=== TURMAS ===\n");
            for (Turma turma : turmas) {
                debug.append("ID: ").append(turma.getId())
                     .append(" | Nome: ").append(turma.getNomeTurma())
                     .append(" | Nível: ").append(turma.getNivelProficiencia()).append("\n");
            }
            
            return debug.toString();
        } catch (Exception e) {
            logger.error("Erro no debug: ", e);
            return "Erro no debug: " + e.getMessage();
        }
    }

    // Endpoints para avaliações (redirecionamento do boletim)
    @GetMapping("/avaliacoes")
    public String avaliacoesMenu(Model model) {
        logger.info("Redirecionando para menu de avaliações");
        model.addAttribute("totalAvaliacoes", avaliacaoService.contarTotalAvaliacoes());
        model.addAttribute("avaliacoesAtivas", avaliacaoService.contarAvaliacoesAtivas());
        model.addAttribute("avaliacoesFinalizadas", avaliacaoService.contarAvaliacoesFinalizadas());
        return "avaliacoes/avaliacoes-menu";
    }

    @GetMapping("/avaliacoes/lista")
    public String listarAvaliacoes(Model model,
                                   @RequestParam(value = "nome", required = false) String nome,
                                   @RequestParam(value = "turmaId", required = false) Long turmaId,
                                   @RequestParam(value = "ativa", required = false) Boolean ativa) {
        logger.info("Redirecionando para lista de avaliações com filtros: nome={}, turmaId={}, ativa={}", nome, turmaId, ativa);
        
        try {
            List<br.com.arirang.plataforma.dto.AvaliacaoDTO> avaliacoes;

            if (turmaId != null) {
                avaliacoes = avaliacaoService.listarAvaliacoesPorTurma(turmaId);
            } else {
                avaliacoes = avaliacaoService.listarTodasAvaliacoes();
            }

            if (ativa != null) {
                avaliacoes = avaliacoes.stream()
                        .filter(a -> a.ativa() == ativa)
                        .collect(java.util.stream.Collectors.toList());
            }

            List<Turma> turmas = turmaService.listarTodasTurmas();
            List<TurmaDTO> turmasDTO = turmas.stream()
                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("avaliacoes", avaliacoes);
            model.addAttribute("turmas", turmasDTO);
            model.addAttribute("totalAvaliacoes", avaliacoes.size());

            return "avaliacoes/avaliacoes-lista";
        } catch (Exception e) {
            logger.error("Erro ao listar avaliações: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Erro ao carregar avaliações: " + e.getMessage());
            return "error";
        }
    }
}
