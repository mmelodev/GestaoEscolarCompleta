package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.ProfessorDTO;
import br.com.arirang.plataforma.service.ProfessorService;
import br.com.arirang.plataforma.service.TurmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/professores")
public class ProfessorController {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorController.class);

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private TurmaService turmaService;

    @GetMapping
    public String listarProfessores(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "turmaId", required = false) Long turmaId,
            Model model) {
        try {
            List<ProfessorDTO> professores = professorService.listarTodosProfessoresAsDTO();
            
            // Aplicar filtro de busca se fornecido
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                professores = professores.stream()
                        .filter(p -> (p.nomeCompleto() != null && p.nomeCompleto().toLowerCase().contains(searchLower)) ||
                                   (p.email() != null && p.email().toLowerCase().contains(searchLower)) ||
                                   (p.telefone() != null && p.telefone().contains(search)) ||
                                   (p.cargo() != null && p.cargo().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            }
            
            // Aplicar filtro de turma se fornecido
            if (turmaId != null) {
                professores = professores.stream()
                        .filter(p -> p.turmaIds() != null && p.turmaIds().contains(turmaId))
                        .collect(Collectors.toList());
            }
            
            // Carregar turmas para o filtro
            var turmas = turmaService.listarTodasTurmas();
            
            model.addAttribute("professores", professores);
            model.addAttribute("turmas", turmas);
            model.addAttribute("searchTerm", search);
            model.addAttribute("turmaSelecionada", turmaId);
            
            return "professores";
        } catch (Exception e) {
            logger.error("Erro ao listar professores: ", e);
            model.addAttribute("error", "Erro ao carregar lista de professores: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/novo")
    public String novoProfessorForm(Model model) {
        try {
            // Criar um objeto vazio para o formulário (usando o DTO interno para compatibilidade com o form)
            ProfessorFormDTO formDTO = new ProfessorFormDTO();
            model.addAttribute("professor", formDTO);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "professor-form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de novo professor: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProfessorForm(@PathVariable Long id, Model model) {
        try {
            ProfessorDTO professorDTO = professorService.buscarProfessorPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + id));
            
            // Converter ProfessorDTO para ProfessorFormDTO para o formulário
            ProfessorFormDTO formDTO = new ProfessorFormDTO();
            formDTO.setId(professorDTO.id());
            formDTO.setNomeCompleto(professorDTO.nomeCompleto());
            formDTO.setDataNascimento(professorDTO.dataNascimento() != null ? 
                    professorDTO.dataNascimento().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
            formDTO.setRg(professorDTO.rg());
            formDTO.setCpf(professorDTO.cpf());
            formDTO.setEmail(professorDTO.email());
            formDTO.setTelefone(professorDTO.telefone());
            formDTO.setCargo(professorDTO.cargo());
            formDTO.setFormacao(professorDTO.formacao());
            formDTO.setTurmaIds(professorDTO.turmaIds() != null ? professorDTO.turmaIds() : new java.util.ArrayList<>());
            
            model.addAttribute("professor", formDTO);
            model.addAttribute("turmas", turmaService.listarTodasTurmas());
            return "professor-form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição para professor ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String criarProfessor(@ModelAttribute("professor") ProfessorFormDTO formDTO, 
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        logger.info("Recebendo requisição para criar professor. Nome: {}", formDTO != null ? formDTO.getNomeCompleto() : "null");
        try {
            // Validar campos obrigatórios
            if (formDTO == null || formDTO.getNomeCompleto() == null || formDTO.getNomeCompleto().trim().isEmpty()) {
                logger.warn("Nome completo não fornecido ou vazio");
                bindingResult.rejectValue("nomeCompleto", "error.nomeCompleto", "Nome completo é obrigatório");
            }
            
            // Se houver erros de validação, retornar para o formulário
            if (bindingResult.hasErrors()) {
                logger.warn("Erros de validação encontrados: {}", bindingResult.getAllErrors());
                model.addAttribute("professor", formDTO);
                model.addAttribute("turmas", turmaService.listarTodasTurmas());
                return "professor-form";
            }
            
            // Converter ProfessorFormDTO para ProfessorDTO
            LocalDate dataNascimento = null;
            if (formDTO.getDataNascimento() != null && !formDTO.getDataNascimento().trim().isEmpty()) {
                try {
                    dataNascimento = LocalDate.parse(formDTO.getDataNascimento(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    logger.warn("Erro ao parsear data de nascimento: {}", formDTO.getDataNascimento());
                }
            }
            
            // Normalizar campos opcionais: converter strings vazias para null
            String telefone = formDTO.getTelefone() != null && !formDTO.getTelefone().trim().isEmpty() 
                    ? formDTO.getTelefone().trim() : null;
            String email = formDTO.getEmail() != null && !formDTO.getEmail().trim().isEmpty() 
                    ? formDTO.getEmail().trim() : null;
            String cpf = formDTO.getCpf() != null && !formDTO.getCpf().trim().isEmpty() 
                    ? formDTO.getCpf().trim() : null;
            String rg = formDTO.getRg() != null && !formDTO.getRg().trim().isEmpty() 
                    ? formDTO.getRg().trim() : null;
            String cargo = formDTO.getCargo() != null && !formDTO.getCargo().trim().isEmpty() 
                    ? formDTO.getCargo().trim() : null;
            String formacao = formDTO.getFormacao() != null && !formDTO.getFormacao().trim().isEmpty() 
                    ? formDTO.getFormacao().trim() : null;
            
            // Processar turmaIds - garantir que seja uma lista mesmo se vazia
            List<Long> turmaIds = formDTO.getTurmaIds();
            if (turmaIds == null) {
                turmaIds = new java.util.ArrayList<>();
            }
            
            ProfessorDTO professorDTO = new ProfessorDTO(
                    null,
                    formDTO.getNomeCompleto(),
                    dataNascimento,
                    rg,
                    cpf,
                    email,
                    telefone,
                    cargo,
                    formacao,
                    turmaIds,
                    null
            );
            
            logger.info("Criando professor: nome={}, turmaIds={}", formDTO.getNomeCompleto(), turmaIds);
            professorService.criarProfessor(professorDTO);
            logger.info("Professor criado com sucesso! Redirecionando para lista de professores.");
            redirectAttributes.addFlashAttribute("success", "Professor cadastrado com sucesso!");
            return "redirect:/professores";
        } catch (Exception e) {
            logger.error("Erro ao criar professor: ", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Erro desconhecido ao criar professor";
            redirectAttributes.addFlashAttribute("error", "Erro ao criar professor: " + errorMessage);
            return "redirect:/professores/novo";
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarProfessor(@PathVariable Long id, 
                                     @ModelAttribute("professor") ProfessorFormDTO formDTO,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Converter ProfessorFormDTO para ProfessorDTO
            LocalDate dataNascimento = null;
            if (formDTO.getDataNascimento() != null && !formDTO.getDataNascimento().trim().isEmpty()) {
                try {
                    dataNascimento = LocalDate.parse(formDTO.getDataNascimento(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    logger.warn("Erro ao parsear data de nascimento: {}", formDTO.getDataNascimento());
                }
            }
            
            // Normalizar campos opcionais: converter strings vazias para null
            String telefone = formDTO.getTelefone() != null && !formDTO.getTelefone().trim().isEmpty() 
                    ? formDTO.getTelefone().trim() : null;
            String email = formDTO.getEmail() != null && !formDTO.getEmail().trim().isEmpty() 
                    ? formDTO.getEmail().trim() : null;
            String cpf = formDTO.getCpf() != null && !formDTO.getCpf().trim().isEmpty() 
                    ? formDTO.getCpf().trim() : null;
            String rg = formDTO.getRg() != null && !formDTO.getRg().trim().isEmpty() 
                    ? formDTO.getRg().trim() : null;
            String cargo = formDTO.getCargo() != null && !formDTO.getCargo().trim().isEmpty() 
                    ? formDTO.getCargo().trim() : null;
            String formacao = formDTO.getFormacao() != null && !formDTO.getFormacao().trim().isEmpty() 
                    ? formDTO.getFormacao().trim() : null;
            
            ProfessorDTO professorDTO = new ProfessorDTO(
                    id,
                    formDTO.getNomeCompleto(),
                    dataNascimento,
                    rg,
                    cpf,
                    email,
                    telefone,
                    cargo,
                    formacao,
                    formDTO.getTurmaIds() != null ? formDTO.getTurmaIds() : new java.util.ArrayList<>(),
                    null
            );
            
            professorService.atualizarProfessor(id, professorDTO);
            redirectAttributes.addFlashAttribute("success", "Professor atualizado com sucesso!");
            return "redirect:/professores";
        } catch (Exception e) {
            logger.error("Erro ao atualizar professor ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar professor: " + e.getMessage());
            return "redirect:/professores/editar/" + id;
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarProfessorConfirm(@PathVariable Long id, Model model) {
        try {
            ProfessorDTO professor = professorService.buscarProfessorPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + id));
            model.addAttribute("professor", professor);
            return "professor-delete";
        } catch (Exception e) {
            logger.error("Erro ao carregar confirmação de deleção para professor ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar confirmação: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/deletar/{id}")
    public String deletarProfessor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            professorService.deletarProfessor(id);
            redirectAttributes.addFlashAttribute("success", "Professor deletado com sucesso!");
            return "redirect:/professores";
        } catch (Exception e) {
            logger.error("Erro ao deletar professor ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar professor: " + e.getMessage());
            return "redirect:/professores";
        }
    }

    // DTO para o formulário (compatível com Thymeleaf)
    public static class ProfessorFormDTO {
        private Long id;
        private String nomeCompleto;
        private String dataNascimento;
        private String rg;
        private String cpf;
        private String email;
        private String telefone;
        private String cargo;
        private String formacao;
        private List<Long> turmaIds;

        public ProfessorFormDTO() {
        }

        public ProfessorFormDTO(Long id, String nomeCompleto, String dataNascimento, String rg, 
                           String cpf, String email, String telefone, String cargo, 
                           String formacao) {
            this.id = id;
            this.nomeCompleto = nomeCompleto;
            this.dataNascimento = dataNascimento;
            this.rg = rg;
            this.cpf = cpf;
            this.email = email;
            this.telefone = telefone;
            this.cargo = cargo;
            this.formacao = formacao;
        }

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNomeCompleto() { return nomeCompleto; }
        public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
        public String getDataNascimento() { return dataNascimento; }
        public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
        public String getRg() { return rg; }
        public void setRg(String rg) { this.rg = rg; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
        public String getCargo() { return cargo; }
        public void setCargo(String cargo) { this.cargo = cargo; }
        public String getFormacao() { return formacao; }
        public void setFormacao(String formacao) { this.formacao = formacao; }
        public List<Long> getTurmaIds() { return turmaIds; }
        public void setTurmaIds(List<Long> turmaIds) { this.turmaIds = turmaIds; }
    }
}

