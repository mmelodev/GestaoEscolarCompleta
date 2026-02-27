package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Endereco;
import br.com.arirang.plataforma.mapper.AlunoMapper;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.service.ContratoService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private static final Logger logger = LoggerFactory.getLogger(AlunoController.class);

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private AlunoMapper alunoMapper;

    private AlunoDTO convertToDTO(Aluno aluno) { return alunoMapper.toDto(aluno); }

    /**
     * Garante que o endereco esteja sempre inicializado no AlunoDTO
     * para evitar erros de binding com propriedades aninhadas em records
     */
    private AlunoDTO ensureEnderecoInitialized(AlunoDTO aluno) {
        if (aluno != null && aluno.endereco() == null) {
            // Criar novo DTO com endereco inicializado
            return new AlunoDTO(
                aluno.id(),
                aluno.nomeCompleto(),
                aluno.email(),
                aluno.cpf(),
                aluno.rg(),
                aluno.orgaoExpeditorRg(),
                aluno.nacionalidade(),
                aluno.uf(),
                aluno.telefone(),
                aluno.dataNascimento(),
                aluno.nomeSocial(),
                aluno.apelido(),
                aluno.genero(),
                aluno.situacao(),
                aluno.ultimoNivel(),
                new Endereco("", "", "", "", "", "", ""), // Inicializar endereco vazio
                aluno.grauParentesco(),
                aluno.responsavelFinanceiro(),
                aluno.nomeResponsavel(),
                aluno.cpfResponsavel(),
                aluno.telefoneResponsavel(),
                aluno.emailResponsavel(),
                aluno.turmaIds(),
                aluno.turmaNomes(),
                aluno.idiomas()
            );
        }
        return aluno;
    }

    @GetMapping("/menu")
    public String menuAlunos(Model model) {
        try {
            return "alunos-menu";
        } catch (Exception e) {
            logger.error("Erro ao carregar menu de alunos: ", e);
            model.addAttribute("error", "Erro ao carregar menu: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/lista")
    public String listarAlunos(@RequestParam(value = "turmaId", required = false) Long turmaId,
                              @RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "situacao", required = false) String situacao,
                              Model model) {
        try {
            List<AlunoDTO> alunos = (turmaId == null
                    ? alunoService.listarTodosAlunos()
                    : alunoService.listarAlunosPorTurma(turmaId))
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // Aplicar filtro de busca se fornecido
            if (search != null && !search.trim().isEmpty()) {
                alunos = alunos.stream()
                        .filter(aluno -> aluno.nomeCompleto().toLowerCase().contains(search.toLowerCase()) ||
                                       (aluno.email() != null && aluno.email().toLowerCase().contains(search.toLowerCase())) ||
                                       (aluno.telefone() != null && aluno.telefone().contains(search)))
                        .collect(Collectors.toList());
            }
            
            // Aplicar filtro de situação se fornecido
            if (situacao != null && !situacao.trim().isEmpty()) {
                alunos = alunos.stream()
                        .filter(aluno -> situacao.equalsIgnoreCase(aluno.situacao()))
                        .collect(Collectors.toList());
            }
            
            model.addAttribute("alunos", alunos);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            model.addAttribute("turmaSelecionada", turmaId);
            model.addAttribute("searchTerm", search);
            model.addAttribute("situacaoSelecionada", situacao);
        } catch (Exception e) {
            logger.error("Erro ao carregar os alunos: ", e);
            model.addAttribute("error", "Erro ao carregar os alunos: " + e.getMessage());
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
        }
        return "alunos";
    }

    @GetMapping("/relatorio")
    public String relatorioPorTurma(@RequestParam(value = "turmaId", required = false) Long turmaId, Model model) {
        try {
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            if (turmaId != null) {
                List<AlunoDTO> alunos = alunoService.listarAlunosPorTurma(turmaId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                model.addAttribute("alunos", alunos);
                model.addAttribute("turmaSelecionada", turmaId);
            }
            return "alunos-relatorio";
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório por turma: ", e);
            model.addAttribute("error", "Erro ao gerar relatório: " + e.getMessage());
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "alunos-relatorio";
        }
    }

    @GetMapping("/novo")
    public String novoAlunoForm(Model model) {
        model.addAttribute("aluno", new AlunoDTO(
                null,                        // id
                "",                          // nomeCompleto
                "",                          // email
                "",                          // cpf
                "",                          // rg
                "",                          // orgaoExpeditorRg
                "",                          // nacionalidade
                "",                          // uf
                "",                          // telefone
                LocalDate.now(),             // dataNascimento
                "",                          // nomeSocial
                "",                          // apelido
                "",                          // genero
                "",                          // situacao
                "",                          // ultimoNivel
                new Endereco("", "", "", "", "", "", ""), // endereco inicializado
                "",                          // grauParentesco
                false,                       // responsavelFinanceiro
                null,                        // nomeResponsavel
                null,                        // cpfResponsavel
                null,                        // telefoneResponsavel
                null,                        // emailResponsavel
                Collections.<Long>emptyList(),     // turmaIds
                Collections.<String>emptyList(),   // turmaNomes
                Collections.<String>emptyList()    // idiomas
        ));
        model.addAttribute("isNew", true);
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        return "aluno-form";
    }

    @PostMapping
    public String criarAlunoMVC(@Valid @ModelAttribute("aluno") AlunoDTO novoAluno, 
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            logger.warn("Erros de validação ao criar aluno: {}", bindingResult.getAllErrors());
            // Log específico para data de nascimento
            if (bindingResult.hasFieldErrors("dataNascimento")) {
                logger.error("Erro na data de nascimento: {}", bindingResult.getFieldError("dataNascimento"));
            }
            // Log específico para CPF
            if (bindingResult.hasFieldErrors("cpf")) {
                logger.error("Erro no CPF: {} - Valor recebido: '{}'", 
                    bindingResult.getFieldError("cpf"), 
                    novoAluno.cpf() != null ? novoAluno.cpf() : "null");
            }
            // Garantir que endereco está inicializado para evitar erro de binding
            novoAluno = ensureEnderecoInitialized(novoAluno);
            model.addAttribute("aluno", novoAluno);
            model.addAttribute("isNew", true);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-form";
        }
        try {
            logger.debug("Criando aluno com DTO: {}", novoAluno);
            logger.debug("Data de nascimento recebida: {}", novoAluno.dataNascimento());
            logger.debug("Turma IDs recebidos: {}", novoAluno.turmaIds().stream().map(Object::toString).collect(Collectors.joining(", ")));
            
            // Idiomas agora são definidos na turma, não no aluno
            // Garantir que idiomas seja uma lista vazia
            novoAluno = new AlunoDTO(
                novoAluno.id(), novoAluno.nomeCompleto(), novoAluno.email(), novoAluno.cpf(),
                novoAluno.rg(), novoAluno.orgaoExpeditorRg(), novoAluno.nacionalidade(), novoAluno.uf(),
                novoAluno.telefone(), novoAluno.dataNascimento(), novoAluno.nomeSocial(), novoAluno.apelido(),
                novoAluno.genero(), novoAluno.situacao(), novoAluno.ultimoNivel(), novoAluno.endereco(),
                novoAluno.grauParentesco(), novoAluno.responsavelFinanceiro(), novoAluno.nomeResponsavel(),
                novoAluno.cpfResponsavel(), novoAluno.telefoneResponsavel(), novoAluno.emailResponsavel(),
                novoAluno.turmaIds(), novoAluno.turmaNomes(), Collections.emptyList()
            );
            
            // Validação adicional da data de nascimento
            if (novoAluno.dataNascimento() == null) {
                logger.error("Data de nascimento é nula no DTO recebido");
                // Garantir que endereco está inicializado para evitar erro de binding
                novoAluno = ensureEnderecoInitialized(novoAluno);
                model.addAttribute("aluno", novoAluno);
                model.addAttribute("error", "Data de nascimento é obrigatória");
                model.addAttribute("isNew", true);
                model.addAttribute("turmas", turmaService.listarTodasTurmas());
                return "aluno-form";
            }
            
            alunoService.criarAluno(novoAluno);
            return "redirect:/alunos/lista?success=Aluno criado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao criar aluno: ", e);
            // Garantir que endereco está inicializado para evitar erro de binding
            novoAluno = ensureEnderecoInitialized(novoAluno);
            model.addAttribute("aluno", novoAluno);
            model.addAttribute("error", "Erro ao criar aluno: " + e.getMessage());
            model.addAttribute("isNew", true);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarAlunoForm(@PathVariable Long id, Model model) {
        try {
            AlunoDTO aluno = alunoService.buscarAlunoPorId(id)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + id));
            // Garantir que endereco está inicializado
            aluno = ensureEnderecoInitialized(aluno);
            model.addAttribute("aluno", aluno);
            model.addAttribute("isNew", false);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar o formulário: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarAlunoMVC(@PathVariable Long id,
                                   @Valid @ModelAttribute("aluno") AlunoDTO alunoAtualizado,
                                   BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Garantir que endereco está inicializado para evitar erro de binding
            alunoAtualizado = ensureEnderecoInitialized(alunoAtualizado);
            model.addAttribute("aluno", alunoAtualizado);
            model.addAttribute("isNew", false);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-form";
        }
        try {
            logger.debug("Atualizando aluno ID {} com DTO: {}", id, alunoAtualizado);
            
            // Idiomas agora são definidos na turma, não no aluno
            // Garantir que idiomas seja uma lista vazia
            alunoAtualizado = new AlunoDTO(
                alunoAtualizado.id(), alunoAtualizado.nomeCompleto(), alunoAtualizado.email(), alunoAtualizado.cpf(),
                alunoAtualizado.rg(), alunoAtualizado.orgaoExpeditorRg(), alunoAtualizado.nacionalidade(), alunoAtualizado.uf(),
                alunoAtualizado.telefone(), alunoAtualizado.dataNascimento(), alunoAtualizado.nomeSocial(), alunoAtualizado.apelido(),
                alunoAtualizado.genero(), alunoAtualizado.situacao(), alunoAtualizado.ultimoNivel(), alunoAtualizado.endereco(),
                alunoAtualizado.grauParentesco(), alunoAtualizado.responsavelFinanceiro(), alunoAtualizado.nomeResponsavel(),
                alunoAtualizado.cpfResponsavel(), alunoAtualizado.telefoneResponsavel(), alunoAtualizado.emailResponsavel(),
                alunoAtualizado.turmaIds(), alunoAtualizado.turmaNomes(), Collections.emptyList()
            );
            
            alunoService.atualizarAluno(id, alunoAtualizado);
            return "redirect:/alunos/lista?success=Aluno atualizado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao atualizar aluno com ID {}: ", id, e);
            // Garantir que endereco está inicializado para evitar erro de binding
            alunoAtualizado = ensureEnderecoInitialized(alunoAtualizado);
            model.addAttribute("aluno", alunoAtualizado);
            model.addAttribute("error", "Erro ao atualizar aluno: " + e.getMessage());
            model.addAttribute("isNew", false);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-form";
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarAlunoConfirm(@PathVariable Long id, Model model) {
        try {
            AlunoDTO aluno = alunoService.buscarAlunoPorId(id)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + id));
            model.addAttribute("aluno", aluno);
            return "aluno-delete";
        } catch (Exception e) {
            logger.error("Erro ao carregar confirmação de deleção para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar a confirmação: " + e.getMessage());
            return "error";
        }
    }

    @DeleteMapping("/{id}")
    public String deletarAluno(@PathVariable Long id, Model model) {
        try {
            logger.debug("Deletando aluno com ID: {}", id);
            alunoService.deletarAluno(id);
            return "redirect:/alunos/lista";
        } catch (Exception e) {
            logger.error("Erro ao deletar aluno com ID {}: ", id, e);
            model.addAttribute("error", "Erro ao deletar aluno: " + e.getMessage());
            return "aluno-delete"; // Retorna à página de confirmação com erro
        }
    }

    @PostMapping("/{id}")
    public String deletarAlunoMVC(@PathVariable Long id) {
        try {
            alunoService.deletarAluno(id);
        } catch (Exception e) {
            logger.error("Erro ao deletar aluno com ID {}: ", id, e);
            // Optionally add error to flash attributes for redirect
        }
        return "redirect:/alunos/lista";
    }

    @GetMapping("/turma/{id}")
    public String associarTurmaForm(@PathVariable Long id, Model model) {
        try {
            AlunoDTO aluno = alunoService.buscarAlunoPorId(id)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + id));
            model.addAttribute("aluno", aluno);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "aluno-turma";
        } catch (Exception e) {
            logger.error("Erro ao carregar associação de turma para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar a associação: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/turmas")
    public String salvarAssociacaoTurma(@PathVariable Long id, @RequestParam("turmaId") Long turmaId, RedirectAttributes redirectAttributes) {
        try {
            alunoService.associarTurma(id, turmaId);
            redirectAttributes.addFlashAttribute("success", "Aluno vinculado à turma com sucesso");
            return "redirect:/alunos/lista";
        } catch (jakarta.validation.ConstraintViolationException e) {
            logger.error("Erro de validação ao associar turma para ID {}: ", id, e);
            StringBuilder errorMsg = new StringBuilder("Erro ao associar turma: dados do aluno inválidos (CPF ou telefone). ");
            errorMsg.append("Por favor, corrija os dados do aluno antes de associá-lo a uma turma.");
            redirectAttributes.addFlashAttribute("error", errorMsg.toString());
            return "redirect:/alunos/lista";
        } catch (Exception e) {
            logger.error("Erro ao associar turma para ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao associar turma: " + e.getMessage());
            return "redirect:/alunos/lista";
        }
    }

    @PostMapping("/{id}/turmas/{turmaId}/remover")
    public String removerTurma(@PathVariable Long id, @PathVariable Long turmaId, 
                               @RequestParam(value = "redirect", required = false, defaultValue = "/alunos/lista") String redirectPath,
                               RedirectAttributes redirectAttributes) {
        try {
            alunoService.removerTurma(id, turmaId);
            redirectAttributes.addFlashAttribute("success", "Aluno removido da turma com sucesso. O aluno ficou inativo por não ter mais turmas.");
            return "redirect:" + redirectPath;
        } catch (Exception e) {
            logger.error("Erro ao remover aluno ID {} da turma ID {}: ", id, turmaId, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao remover aluno da turma: " + e.getMessage());
            return "redirect:" + redirectPath;
        }
    }

    @GetMapping("/situacao/{id}")
    public String alterarSituacaoForm(@PathVariable Long id, Model model) {
        try {
            AlunoDTO aluno = alunoService.buscarAlunoPorId(id)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + id));
            model.addAttribute("aluno", aluno);
            return "aluno-situacao";
        } catch (Exception e) {
            logger.error("Erro ao carregar alteração de situação para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar a alteração: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/situacao")
    public String salvarSituacao(@PathVariable Long id, @RequestParam("situacao") String situacao, RedirectAttributes redirectAttributes) {
        try {
            alunoService.alterarSituacao(id, situacao);
            redirectAttributes.addFlashAttribute("success", "Situação do aluno alterada com sucesso");
            return "redirect:/alunos/lista";
        } catch (Exception e) {
            logger.error("Erro ao alterar situação para ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/alunos/lista";
        }
    }

    /**
     * Gera contrato para aluno após vinculação à turma
     */
    @PostMapping("/{id}/gerar-contrato")
    public String gerarContratoParaAluno(@PathVariable Long id, @RequestParam("turmaId") Long turmaId) {
        try {
            contratoService.gerarContratoRapido(id, turmaId);
            return "redirect:/contratos?success=Contrato gerado automaticamente";
        } catch (Exception e) {
            logger.error("Erro ao gerar contrato para aluno ID {} e turma ID {}: ", id, turmaId, e);
            return "redirect:/alunos/lista?error=" + e.getMessage();
        }
    }

    /**
     * Visualiza contratos do aluno
     */
    @GetMapping("/{id}/contratos")
    public String visualizarContratosAluno(@PathVariable Long id, Model model) {
        // Templates de contrato fora de `templates/contratos/pdf/*` foram descontinuados.
        // Direcionar para a listagem/gestão de contratos v2.
        return "redirect:/contratos-v2";
    }

}
