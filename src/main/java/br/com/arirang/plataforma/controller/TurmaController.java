package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.enums.Turno;
import br.com.arirang.plataforma.enums.Formato;
import br.com.arirang.plataforma.enums.Modalidade;
import br.com.arirang.plataforma.service.ProfessorService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.repository.ContratoRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/turmas")
public class TurmaController {

    private static final Logger logger = LoggerFactory.getLogger(TurmaController.class);

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private ContratoRepository contratoRepository;
    
    @Autowired
    private br.com.arirang.plataforma.service.PadraoBoletimService padraoBoletimService;

    private static final Path CALENDARIO_UPLOAD_DIR = Paths.get("uploads/calendarios-turmas");
    
    // Lista de idiomas disponíveis
    private static final List<String> IDIOMAS_DISPONIVEIS = Arrays.asList(
        "Coreano", "Francês", "Inglês", "Mandarim", "Japonês", "Espanhol", "Outro Idioma?"
    );
    
    // Constante para identificar a opção "Outro Idioma?"
    public static final String OUTRO_IDIOMA = "Outro Idioma?";

    private AlunoDTO convertAlunoToDTO(Aluno aluno) {
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
                aluno.getResponsavel() != null && aluno.isResponsavelFinanceiro() ? aluno.getResponsavel().getNomeCompleto() : null,
                aluno.getResponsavel() != null && aluno.isResponsavelFinanceiro() ? aluno.getResponsavel().getCpf() : null,
                aluno.getResponsavel() != null && aluno.isResponsavelFinanceiro() ? aluno.getResponsavel().getTelefone() : null,
                aluno.getResponsavel() != null && aluno.isResponsavelFinanceiro() ? aluno.getResponsavel().getEmail() : null,
                aluno.getTurmas() != null ? aluno.getTurmas().stream().map(Turma::getId).collect(Collectors.toList()) : Collections.<Long>emptyList(),
                aluno.getTurmas() != null ? aluno.getTurmas().stream().map(Turma::getNomeTurma).collect(Collectors.toList()) : Collections.<String>emptyList(),
                aluno.getIdiomas() != null ? aluno.getIdiomas() : Collections.<String>emptyList()
        );
    }

    @GetMapping("/novo")
    public String novaTurmaForm(Model model) {
        model.addAttribute("turma", new TurmaDTO(
                null,                      // id
                "",                        // nomeTurma
                null,                      // idioma
                null,                      // professorResponsavelId
                "",                        // nivelProficiencia
                "",                        // diaTurma
                null,                      // turno
                null,                      // formato
                null,                      // modalidade
                "",                        // realizador
                "",                        // horaInicio
                "",                        // horaTermino
                "",                        // anoSemestre
                null,                      // cargaHorariaTotal
                null,                      // quantidadeAulas
                null,                      // calendarioPdf
                LocalDate.now(),           // inicioTurma (ajustado para LocalDate)
                LocalDate.now().plusDays(7), // terminoTurma
                "Prevista",                // situacaoTurma
                Collections.emptyList()    // alunoIds
        ));
        model.addAttribute("isNew", true);
        model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
        model.addAttribute("turnos", Turno.values());
        model.addAttribute("formatos", Formato.values());
        model.addAttribute("modalidades", Modalidade.values());
        model.addAttribute("idiomasDisponiveis", IDIOMAS_DISPONIVEIS);
        return "turma-form";
    }

    @PostMapping
    public String criarTurmaMVC(
            @Valid @ModelAttribute("turma") TurmaDTO novaTurma, 
            BindingResult bindingResult,
            @RequestParam(value = "calendarioPdfFile", required = false) MultipartFile calendarioPdfFile,
            @RequestParam(value = "outroIdioma", required = false) String outroIdioma,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Processar "Outro Idioma?" ANTES da validação
        if (novaTurma.idioma() != null && novaTurma.idioma().equals(OUTRO_IDIOMA)) {
            if (outroIdioma != null && !outroIdioma.trim().isEmpty()) {
                // Substituir "Outro Idioma?" pelo idioma digitado
                novaTurma = new TurmaDTO(
                    novaTurma.id(), novaTurma.nomeTurma(), outroIdioma.trim(),
                    novaTurma.professorResponsavelId(), novaTurma.nivelProficiencia(),
                    novaTurma.diaTurma(), novaTurma.turno(), novaTurma.formato(),
                    novaTurma.modalidade(), novaTurma.realizador(), novaTurma.horaInicio(),
                    novaTurma.horaTermino(), novaTurma.anoSemestre(), novaTurma.cargaHorariaTotal(),
                    novaTurma.quantidadeAulas(), novaTurma.calendarioPdf(), novaTurma.inicioTurma(),
                    novaTurma.terminoTurma(), novaTurma.situacaoTurma(), novaTurma.alunoIds()
                );
            } else {
                bindingResult.rejectValue("idioma", "NotBlank", "Por favor, especifique o idioma no campo de texto.");
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", true);
            model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
            model.addAttribute("turnos", Turno.values());
            model.addAttribute("formatos", Formato.values());
            model.addAttribute("modalidades", Modalidade.values());
            model.addAttribute("idiomasDisponiveis", IDIOMAS_DISPONIVEIS);
            return "turma-form";
        }
        try {
            // Validar e salvar PDF de calendário
            if (calendarioPdfFile == null || calendarioPdfFile.isEmpty()) {
                model.addAttribute("error", "Calendário PDF é obrigatório para criar uma turma.");
                model.addAttribute("isNew", true);
                model.addAttribute("turma", novaTurma);
                model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                model.addAttribute("turnos", Turno.values());
                model.addAttribute("formatos", Formato.values());
                model.addAttribute("modalidades", Modalidade.values());
                return "turma-form";
            }
            
            // Validar que é um PDF
            String originalFilename = calendarioPdfFile.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                model.addAttribute("error", "O arquivo deve ser um PDF (.pdf)");
                model.addAttribute("isNew", true);
                model.addAttribute("turma", novaTurma);
                model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                model.addAttribute("turnos", Turno.values());
                model.addAttribute("formatos", Formato.values());
                model.addAttribute("modalidades", Modalidade.values());
                return "turma-form";
            }
            
            // Criar DTO temporário com valor placeholder para calendarioPdf
            // (será atualizado após salvar o arquivo)
            TurmaDTO turmaComCalendarioTemporario = new TurmaDTO(
                novaTurma.id(),
                novaTurma.nomeTurma(),
                novaTurma.idioma(),
                novaTurma.professorResponsavelId(),
                novaTurma.nivelProficiencia(),
                novaTurma.diaTurma(),
                novaTurma.turno(),
                novaTurma.formato(),
                novaTurma.modalidade(),
                novaTurma.realizador(),
                novaTurma.horaInicio(),
                novaTurma.horaTermino(),
                novaTurma.anoSemestre(),
                novaTurma.cargaHorariaTotal(),
                novaTurma.quantidadeAulas(),
                "PENDING_UPLOAD", // Valor temporário - será substituído após upload
                novaTurma.inicioTurma(),
                novaTurma.terminoTurma(),
                novaTurma.situacaoTurma(),
                novaTurma.alunoIds()
            );
            
            // Criar turma primeiro para obter o ID
            Turma turmaCriada = turmaService.criarTurma(turmaComCalendarioTemporario);
            
            // Salvar PDF de calendário
            try {
                Files.createDirectories(CALENDARIO_UPLOAD_DIR);
                String filename = "turma_" + turmaCriada.getId() + "_calendario.pdf";
                Path target = CALENDARIO_UPLOAD_DIR.resolve(filename).normalize();
                
                // Validar path (prevenir path traversal)
                if (!target.startsWith(CALENDARIO_UPLOAD_DIR.normalize())) {
                    throw new RuntimeException("Tentativa de acesso a caminho não autorizado");
                }
                
                Files.copy(calendarioPdfFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                
                // Atualizar turma com o caminho do PDF
                TurmaDTO turmaAtualizada = new TurmaDTO(
                    turmaCriada.getId(),
                    turmaCriada.getNomeTurma(),
                    turmaCriada.getIdioma(),
                    turmaCriada.getProfessorResponsavel() != null ? turmaCriada.getProfessorResponsavel().getId() : null,
                    turmaCriada.getNivelProficiencia(),
                    turmaCriada.getDiaTurma(),
                    turmaCriada.getTurno(),
                    turmaCriada.getFormato(),
                    turmaCriada.getModalidade(),
                    turmaCriada.getRealizador(),
                    turmaCriada.getHoraInicio(),
                    turmaCriada.getHoraTermino(),
                    turmaCriada.getAnoSemestre(),
                    turmaCriada.getCargaHorariaTotal(),
                    turmaCriada.getQuantidadeAulas(),
                    filename, // calendarioPdf
                    turmaCriada.getInicioTurma(),
                    turmaCriada.getTerminoTurma(),
                    turmaCriada.getSituacaoTurma(),
                    turmaCriada.getAlunos() != null ? turmaCriada.getAlunos().stream().map(a -> a.getId()).collect(Collectors.toList()) : Collections.emptyList()
                );
                turmaService.atualizarTurma(turmaCriada.getId(), turmaAtualizada);
                
                logger.info("Calendário PDF salvo para turma ID {}: {}", turmaCriada.getId(), filename);
            } catch (Exception e) {
                logger.error("Erro ao salvar calendário PDF para turma ID {}: {}", turmaCriada.getId(), e.getMessage(), e);
                // Deletar turma criada se falhar ao salvar PDF
                try {
                    turmaService.deletarTurma(turmaCriada.getId());
                } catch (Exception deleteEx) {
                    logger.error("Erro ao deletar turma após falha no upload do PDF: {}", deleteEx.getMessage());
                }
                throw new RuntimeException("Erro ao salvar calendário PDF: " + e.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("success", "Turma criada com sucesso!");
            return "redirect:/turmas";
        } catch (Exception e) {
            logger.error("Erro ao criar turma: ", e);
            model.addAttribute("error", "Erro ao criar turma: " + e.getMessage());
            model.addAttribute("isNew", true);
            model.addAttribute("turma", novaTurma);
            model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
            model.addAttribute("turnos", Turno.values());
            model.addAttribute("formatos", Formato.values());
            model.addAttribute("modalidades", Modalidade.values());
            return "turma-form";
        }
    }

    @GetMapping("/verificar-duplicatas")
    public String verificarDuplicatas(Model model) {
        try {
            List<java.util.Map<String, Object>> duplicatas = turmaService.verificarDuplicacaoTurmas();
            model.addAttribute("duplicatas", duplicatas);
            model.addAttribute("temDuplicatas", !duplicatas.isEmpty());
            return "turmas-duplicatas";
        } catch (Exception e) {
            logger.error("Erro ao verificar duplicatas: ", e);
            model.addAttribute("error", "Erro ao verificar duplicatas: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping
    public String listarTodasTurmas(@RequestParam(value = "search", required = false) String search, Model model) {
        try {
            List<Turma> turmas = turmaService.listarTodasTurmas();
            
            // Aplicar filtro de busca se fornecido
            if (search != null && !search.trim().isEmpty()) {
                turmas = turmas.stream()
                        .filter(turma -> {
                            String searchLower = search.toLowerCase();
                            // Busca por ID
                            if (turma.getId().toString().contains(search)) {
                                return true;
                            }
                            // Busca por nome da turma
                            if (turma.getNomeTurma().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            // Busca por professor responsável - não implementado na entidade Turma
                            // if (turma.getProfessorResponsavel() != null && 
                            //     turma.getProfessorResponsavel().getNomeCompleto().toLowerCase().contains(searchLower)) {
                            //     return true;
                            // }
                            // Busca por nível de proficiência
                            if (turma.getNivelProficiencia() != null && 
                                turma.getNivelProficiencia().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            // Busca por turno
                            if (turma.getTurno() != null && 
                                turma.getTurno().getDescricao().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            // Busca por formato
                            if (turma.getFormato() != null && 
                                turma.getFormato().getDescricao().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            // Busca por modalidade
                            if (turma.getModalidade() != null && 
                                turma.getModalidade().getDescricao().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }
            
            model.addAttribute("turmas", turmas);
            model.addAttribute("searchTerm", search);
            return "turmas";
        } catch (Exception e) {
            logger.error("Erro ao listar turmas: ", e);
            model.addAttribute("error", "Erro ao carregar a lista: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarTurmaForm(@PathVariable Long id, Model model) {
        try {
            // Usar método do service que retorna DTO diretamente, garantindo que a conversão
            // aconteça dentro da transação com alunos carregados
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            model.addAttribute("turma", turma);
            model.addAttribute("isNew", false);
            model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
            model.addAttribute("turnos", Turno.values());
            model.addAttribute("formatos", Formato.values());
            model.addAttribute("modalidades", Modalidade.values());
            model.addAttribute("idiomasDisponiveis", IDIOMAS_DISPONIVEIS);
            return "turma-form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de edição para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar o formulário: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}/alunos")
    public String visualizarAlunosTurma(@PathVariable Long id, Model model) {
        try {
            // Usar método do service que retorna DTO diretamente, garantindo que a conversão
            // aconteça dentro da transação com alunos carregados
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            List<AlunoDTO> alunos = turmaService.listarAlunosPorTurma(id)
                    .stream()
                    .map(this::convertAlunoToDTO)
                    .collect(Collectors.toList());
            
            model.addAttribute("turma", turma);
            model.addAttribute("alunos", alunos);
            return "turma-alunos";
        } catch (Exception e) {
            logger.error("Erro ao carregar alunos da turma ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar alunos: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarTurmaMVC(
            @PathVariable Long id, 
            @ModelAttribute("turma") TurmaDTO turmaAtualizada, 
            BindingResult bindingResult,
            @RequestParam(value = "calendarioPdfFile", required = false) MultipartFile calendarioPdfFile,
            @RequestParam(value = "outroIdioma", required = false) String outroIdioma,
            @RequestParam(value = "justificativa", required = true) String justificativa,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request,
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        // Processar "Outro Idioma?"
        if (turmaAtualizada.idioma() != null && turmaAtualizada.idioma().equals(OUTRO_IDIOMA)) {
            if (outroIdioma != null && !outroIdioma.trim().isEmpty()) {
                // Substituir "Outro Idioma?" pelo idioma digitado
                turmaAtualizada = new TurmaDTO(
                    turmaAtualizada.id(), turmaAtualizada.nomeTurma(), outroIdioma.trim(),
                    turmaAtualizada.professorResponsavelId(), turmaAtualizada.nivelProficiencia(),
                    turmaAtualizada.diaTurma(), turmaAtualizada.turno(), turmaAtualizada.formato(),
                    turmaAtualizada.modalidade(), turmaAtualizada.realizador(), turmaAtualizada.horaInicio(),
                    turmaAtualizada.horaTermino(), turmaAtualizada.anoSemestre(), turmaAtualizada.cargaHorariaTotal(),
                    turmaAtualizada.quantidadeAulas(), turmaAtualizada.calendarioPdf(), turmaAtualizada.inicioTurma(),
                    turmaAtualizada.terminoTurma(), turmaAtualizada.situacaoTurma(), turmaAtualizada.alunoIds()
                );
            } else {
                bindingResult.rejectValue("idioma", "NotBlank", "Por favor, especifique o idioma no campo de texto.");
            }
        }
        
        // Log dos dados recebidos ANTES da validação para debug
        logger.info("=== INÍCIO ATUALIZAÇÃO TURMA ID {} ===", id);
        logger.info("DTO recebido - nomeTurma: '{}', nivelProficiencia: '{}', turno: {}, formato: {}, modalidade: {}, situacaoTurma: '{}'", 
                turmaAtualizada.nomeTurma(), 
                turmaAtualizada.nivelProficiencia(), 
                turmaAtualizada.turno(), 
                turmaAtualizada.formato(), 
                turmaAtualizada.modalidade(), 
                turmaAtualizada.situacaoTurma());
        
        // Validar manualmente apenas o campo obrigatório
        if (turmaAtualizada.nomeTurma() == null || turmaAtualizada.nomeTurma().trim().isEmpty()) {
            logger.warn("Nome da turma está vazio ou nulo");
            bindingResult.rejectValue("nomeTurma", "NotBlank", "Nome da turma é obrigatório");
        }
        
        if (bindingResult.hasErrors()) {
            logger.warn("Erros de validação ao atualizar turma ID {}: {}", id, bindingResult.getAllErrors());
            // Carregar turma original para preservar dados não editados
            try {
                TurmaDTO turmaOriginal = turmaService.buscarTurmaPorIdAsDTO(id)
                        .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
                
                // Mesclar dados: usar valores do formulário quando preenchidos, senão usar valores originais
                TurmaDTO turmaMesclada = new TurmaDTO(
                    turmaOriginal.id(),
                    turmaAtualizada.nomeTurma() != null && !turmaAtualizada.nomeTurma().trim().isEmpty() 
                        ? turmaAtualizada.nomeTurma() : turmaOriginal.nomeTurma(),
                    turmaAtualizada.idioma() != null && !turmaAtualizada.idioma().trim().isEmpty() 
                        ? turmaAtualizada.idioma() : turmaOriginal.idioma(),
                    turmaAtualizada.professorResponsavelId() != null 
                        ? turmaAtualizada.professorResponsavelId() : turmaOriginal.professorResponsavelId(),
                    turmaAtualizada.nivelProficiencia() != null && !turmaAtualizada.nivelProficiencia().trim().isEmpty() 
                        ? turmaAtualizada.nivelProficiencia() : turmaOriginal.nivelProficiencia(),
                    turmaAtualizada.diaTurma() != null && !turmaAtualizada.diaTurma().trim().isEmpty() 
                        ? turmaAtualizada.diaTurma() : turmaOriginal.diaTurma(),
                    turmaAtualizada.turno() != null ? turmaAtualizada.turno() : turmaOriginal.turno(),
                    turmaAtualizada.formato() != null ? turmaAtualizada.formato() : turmaOriginal.formato(),
                    turmaAtualizada.modalidade() != null ? turmaAtualizada.modalidade() : turmaOriginal.modalidade(),
                    turmaAtualizada.realizador() != null && !turmaAtualizada.realizador().trim().isEmpty() 
                        ? turmaAtualizada.realizador() : turmaOriginal.realizador(),
                    turmaAtualizada.horaInicio() != null && !turmaAtualizada.horaInicio().trim().isEmpty() 
                        ? turmaAtualizada.horaInicio() : turmaOriginal.horaInicio(),
                    turmaAtualizada.horaTermino() != null && !turmaAtualizada.horaTermino().trim().isEmpty() 
                        ? turmaAtualizada.horaTermino() : turmaOriginal.horaTermino(),
                    turmaAtualizada.anoSemestre() != null && !turmaAtualizada.anoSemestre().trim().isEmpty() 
                        ? turmaAtualizada.anoSemestre() : turmaOriginal.anoSemestre(),
                    turmaAtualizada.cargaHorariaTotal() != null 
                        ? turmaAtualizada.cargaHorariaTotal() : turmaOriginal.cargaHorariaTotal(),
                    turmaAtualizada.quantidadeAulas() != null 
                        ? turmaAtualizada.quantidadeAulas() : turmaOriginal.quantidadeAulas(),
                    turmaOriginal.calendarioPdf(), // Preservar PDF existente se não houver novo upload
                    turmaAtualizada.inicioTurma() != null 
                        ? turmaAtualizada.inicioTurma() : turmaOriginal.inicioTurma(),
                    turmaAtualizada.terminoTurma() != null 
                        ? turmaAtualizada.terminoTurma() : turmaOriginal.terminoTurma(),
                    turmaAtualizada.situacaoTurma() != null && !turmaAtualizada.situacaoTurma().trim().isEmpty() 
                        ? turmaAtualizada.situacaoTurma() : turmaOriginal.situacaoTurma(),
                    turmaAtualizada.alunoIds() != null && !turmaAtualizada.alunoIds().isEmpty() 
                        ? turmaAtualizada.alunoIds() : turmaOriginal.alunoIds()
                );
                
                model.addAttribute("isNew", false);
                model.addAttribute("turma", turmaMesclada);
                model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                model.addAttribute("turnos", Turno.values());
                model.addAttribute("formatos", Formato.values());
                model.addAttribute("modalidades", Modalidade.values());
                model.addAttribute("error", "Por favor, corrija os erros abaixo");
                return "turma-form";
            } catch (Exception e) {
                logger.error("Erro ao carregar turma original para mesclagem: ", e);
                model.addAttribute("isNew", false);
                model.addAttribute("turma", turmaAtualizada);
                model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                model.addAttribute("turnos", Turno.values());
                model.addAttribute("formatos", Formato.values());
                model.addAttribute("modalidades", Modalidade.values());
                model.addAttribute("error", "Erro ao carregar dados da turma: " + e.getMessage());
                return "turma-form";
            }
        }
        
        try {
            // Buscar turma original para preservar dados (especialmente calendarioPdf e alunoIds)
            TurmaDTO turmaOriginalParaMesclagem = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            // Se um novo PDF foi enviado, salvá-lo
            String calendarioPdfPath = null;
            if (calendarioPdfFile != null && !calendarioPdfFile.isEmpty()) {
                // Validar que é um PDF
                String originalFilename = calendarioPdfFile.getOriginalFilename();
                if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
                    try {
                        Files.createDirectories(CALENDARIO_UPLOAD_DIR);
                        String filename = "turma_" + id + "_calendario.pdf";
                        Path target = CALENDARIO_UPLOAD_DIR.resolve(filename).normalize();
                        
                        // Validar path (prevenir path traversal)
                        if (!target.startsWith(CALENDARIO_UPLOAD_DIR.normalize())) {
                            throw new RuntimeException("Tentativa de acesso a caminho não autorizado");
                        }
                        
                        Files.copy(calendarioPdfFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                        calendarioPdfPath = filename;
                        logger.info("Calendário PDF atualizado para turma ID {}: {}", id, filename);
                    } catch (Exception e) {
                        logger.error("Erro ao salvar calendário PDF para turma ID {}: {}", id, e.getMessage(), e);
                        redirectAttributes.addFlashAttribute("error", "Erro ao salvar calendário PDF: " + e.getMessage());
                        return "redirect:/turmas/editar/" + id;
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "O arquivo deve ser um PDF (.pdf)");
                    return "redirect:/turmas/editar/" + id;
                }
            }
            
            // Mesclar dados: preservar calendarioPdf original se não houver novo upload
            // Preservar alunoIds do original (formulário não envia esse campo)
            turmaAtualizada = new TurmaDTO(
                turmaAtualizada.id(),
                turmaAtualizada.nomeTurma() != null && !turmaAtualizada.nomeTurma().trim().isEmpty() 
                    ? turmaAtualizada.nomeTurma() : turmaOriginalParaMesclagem.nomeTurma(),
                turmaAtualizada.idioma() != null && !turmaAtualizada.idioma().trim().isEmpty()
                    ? turmaAtualizada.idioma() : turmaOriginalParaMesclagem.idioma(),
                turmaAtualizada.professorResponsavelId() != null 
                    ? turmaAtualizada.professorResponsavelId() : turmaOriginalParaMesclagem.professorResponsavelId(),
                turmaAtualizada.nivelProficiencia() != null && !turmaAtualizada.nivelProficiencia().trim().isEmpty()
                    ? turmaAtualizada.nivelProficiencia() : turmaOriginalParaMesclagem.nivelProficiencia(),
                turmaAtualizada.diaTurma() != null && !turmaAtualizada.diaTurma().trim().isEmpty()
                    ? turmaAtualizada.diaTurma() : turmaOriginalParaMesclagem.diaTurma(),
                turmaAtualizada.turno() != null ? turmaAtualizada.turno() : turmaOriginalParaMesclagem.turno(),
                turmaAtualizada.formato() != null ? turmaAtualizada.formato() : turmaOriginalParaMesclagem.formato(),
                turmaAtualizada.modalidade() != null ? turmaAtualizada.modalidade() : turmaOriginalParaMesclagem.modalidade(),
                turmaAtualizada.realizador() != null && !turmaAtualizada.realizador().trim().isEmpty()
                    ? turmaAtualizada.realizador() : turmaOriginalParaMesclagem.realizador(),
                turmaAtualizada.horaInicio() != null && !turmaAtualizada.horaInicio().trim().isEmpty()
                    ? turmaAtualizada.horaInicio() : turmaOriginalParaMesclagem.horaInicio(),
                turmaAtualizada.horaTermino() != null && !turmaAtualizada.horaTermino().trim().isEmpty()
                    ? turmaAtualizada.horaTermino() : turmaOriginalParaMesclagem.horaTermino(),
                turmaAtualizada.anoSemestre() != null && !turmaAtualizada.anoSemestre().trim().isEmpty()
                    ? turmaAtualizada.anoSemestre() : turmaOriginalParaMesclagem.anoSemestre(),
                turmaAtualizada.cargaHorariaTotal() != null 
                    ? turmaAtualizada.cargaHorariaTotal() : turmaOriginalParaMesclagem.cargaHorariaTotal(),
                turmaAtualizada.quantidadeAulas() != null 
                    ? turmaAtualizada.quantidadeAulas() : turmaOriginalParaMesclagem.quantidadeAulas(),
                calendarioPdfPath != null ? calendarioPdfPath : turmaOriginalParaMesclagem.calendarioPdf(), // Preservar PDF existente se não houver novo upload
                turmaAtualizada.inicioTurma() != null 
                    ? turmaAtualizada.inicioTurma() : turmaOriginalParaMesclagem.inicioTurma(),
                turmaAtualizada.terminoTurma() != null 
                    ? turmaAtualizada.terminoTurma() : turmaOriginalParaMesclagem.terminoTurma(),
                turmaAtualizada.situacaoTurma() != null && !turmaAtualizada.situacaoTurma().trim().isEmpty()
                    ? turmaAtualizada.situacaoTurma() : turmaOriginalParaMesclagem.situacaoTurma(),
                turmaOriginalParaMesclagem.alunoIds() // Sempre preservar alunoIds do original (formulário não envia)
            );
            
            // Obter usuário atual
            String usuario = authentication != null ? authentication.getName() : "Sistema";
            
            // Log dos dados que serão salvos
            logger.info("Salvando turma ID {} - nomeTurma: '{}', nivelProficiencia: '{}', turno: {}, formato: {}, modalidade: {}, situacaoTurma: '{}'", 
                    id, turmaAtualizada.nomeTurma(), turmaAtualizada.nivelProficiencia(), 
                    turmaAtualizada.turno(), turmaAtualizada.formato(), turmaAtualizada.modalidade(), 
                    turmaAtualizada.situacaoTurma());
            
            // Validar justificativa obrigatória
            if (justificativa == null || justificativa.trim().isEmpty()) {
                logger.warn("Justificativa não fornecida para atualização da turma ID {}", id);
                model.addAttribute("error", "Justificativa é obrigatória para alterações em turmas. Por favor, descreva o motivo da alteração.");
                try {
                    TurmaDTO turmaOriginal = turmaService.buscarTurmaPorIdAsDTO(id)
                            .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
                    model.addAttribute("isNew", false);
                    model.addAttribute("turma", turmaOriginal);
                    model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                    model.addAttribute("turnos", Turno.values());
                    model.addAttribute("formatos", Formato.values());
                    model.addAttribute("modalidades", Modalidade.values());
                    return "turma-form";
                } catch (Exception ex) {
                    logger.error("Erro ao recarregar turma após validação de justificativa: ", ex);
                    redirectAttributes.addFlashAttribute("error", "Justificativa é obrigatória para alterações em turmas.");
                    return "redirect:/turmas/editar/" + id;
                }
            }
            
            // Atualizar turma usando método protegido (com auditoria)
            Turma turmaAtualizadaEntity = turmaService.atualizarTurmaProtegida(
                id, 
                turmaAtualizada, 
                usuario, 
                justificativa, 
                request
            );
            
            // Log dos dados salvos para verificação
            logger.info("Turma ID {} atualizada com sucesso - Nome: '{}', Nível: '{}', Turno: {}, Formato: {}, Modalidade: {}, Situação: '{}', CalendarioPdf: '{}'", 
                    turmaAtualizadaEntity.getId(),
                    turmaAtualizadaEntity.getNomeTurma(),
                    turmaAtualizadaEntity.getNivelProficiencia(),
                    turmaAtualizadaEntity.getTurno(),
                    turmaAtualizadaEntity.getFormato(),
                    turmaAtualizadaEntity.getModalidade(),
                    turmaAtualizadaEntity.getSituacaoTurma(),
                    turmaAtualizadaEntity.getCalendarioPdf());
            logger.info("=== FIM ATUALIZAÇÃO TURMA ID {} ===", id);
            
            // Verificar se os dados foram realmente persistidos recarregando do banco
            try {
                TurmaDTO turmaVerificacao = turmaService.buscarTurmaPorIdAsDTO(id)
                        .orElseThrow(() -> new RuntimeException("Turma não encontrada após atualização"));
                logger.info("VERIFICAÇÃO PÓS-ATUALIZAÇÃO - Turma ID {} - Nome: '{}', Nível: '{}', Turno: {}, Formato: {}, Modalidade: {}, Situação: '{}', CalendarioPdf: '{}'", 
                        turmaVerificacao.id(),
                        turmaVerificacao.nomeTurma(),
                        turmaVerificacao.nivelProficiencia(),
                        turmaVerificacao.turno(),
                        turmaVerificacao.formato(),
                        turmaVerificacao.modalidade(),
                        turmaVerificacao.situacaoTurma(),
                        turmaVerificacao.calendarioPdf());
            } catch (Exception e) {
                logger.error("Erro ao verificar turma após atualização: ", e);
            }
            
            // Buscar protocolo gerado
            String protocolo = turmaService.gerarProtocoloAlteracao(id, usuario);
            
            redirectAttributes.addFlashAttribute("success", "Turma atualizada com sucesso. Protocolo: " + protocolo);
            redirectAttributes.addFlashAttribute("protocolo", protocolo);
            logger.info("Redirecionando para /turmas após atualização bem-sucedida da turma ID {}", id);
            return "redirect:/turmas";
        } catch (Exception e) {
            logger.error("Erro ao atualizar turma com ID {}: ", id, e);
            // Em caso de erro, recarregar turma original
            try {
                TurmaDTO turmaOriginal = turmaService.buscarTurmaPorIdAsDTO(id)
                        .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
                model.addAttribute("isNew", false);
                model.addAttribute("turma", turmaOriginal);
                model.addAttribute("professores", professorService.listarTodosProfessoresAsDTO());
                model.addAttribute("turnos", Turno.values());
                model.addAttribute("formatos", Formato.values());
                model.addAttribute("modalidades", Modalidade.values());
                model.addAttribute("error", "Erro ao atualizar turma: " + e.getMessage());
                return "turma-form";
            } catch (Exception ex) {
                logger.error("Erro ao recarregar turma após falha na atualização: ", ex);
                redirectAttributes.addFlashAttribute("error", "Erro ao atualizar turma: " + e.getMessage());
                return "redirect:/turmas/editar/" + id;
            }
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarTurmaConfirm(@PathVariable Long id, Model model) {
        try {
            // Usar método do service que retorna DTO diretamente, garantindo que a conversão
            // aconteça dentro da transação com alunos carregados
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            // Verificar se há contratos vinculados
            List<br.com.arirang.plataforma.entity.Contrato> contratos = contratoRepository.findByTurmaIdOrderByDataCriacaoDesc(id);
            model.addAttribute("turma", turma);
            model.addAttribute("contratosVinculados", contratos != null ? contratos.size() : 0);
            model.addAttribute("temContratos", contratos != null && !contratos.isEmpty());
            
            return "turma-delete";
        } catch (Exception e) {
            logger.error("Erro ao carregar confirmação de deleção para ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar a confirmação: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/deletar/{id}")
    public String deletarTurmaMVC(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            turmaService.deletarTurma(id);
            redirectAttributes.addFlashAttribute("success", "Turma deletada com sucesso");
            return "redirect:/turmas";
        } catch (Exception e) {
            logger.error("Erro ao deletar turma com ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar turma: " + e.getMessage());
            return "redirect:/turmas";
        }
    }

    @PostMapping("/fechar/{id}")
    public String fecharTurma(@PathVariable Long id, Model model) {
        try {
            turmaService.fecharTurma(id);
            return "redirect:/turmas?success=Turma fechada com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao fechar turma com ID {}: {}", id, e.getMessage());
            return "redirect:/turmas?error=" + e.getMessage();
        }
    }

    @PostMapping("/reabrir/{id}")
    public String reabrirTurma(@PathVariable Long id, Model model) {
        try {
            turmaService.reabrirTurma(id);
            return "redirect:/turmas?success=Turma reaberta com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao reabrir turma com ID {}: {}", id, e.getMessage());
            return "redirect:/turmas?error=" + e.getMessage();
        }
    }

    @GetMapping("/calendario/{id}")
    public ResponseEntity<Resource> obterCalendarioPdf(@PathVariable Long id) {
        try {
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            if (turma.calendarioPdf() == null || turma.calendarioPdf().trim().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = CALENDARIO_UPLOAD_DIR.resolve(turma.calendarioPdf()).normalize();
            
            // Validar path (prevenir path traversal)
            if (!filePath.startsWith(CALENDARIO_UPLOAD_DIR.normalize())) {
                return ResponseEntity.badRequest().build();
            }
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + turma.calendarioPdf() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            logger.error("Erro ao obter calendário PDF para turma ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao obter calendário PDF para turma ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/historico/{id}")
    public String historicoAlteracoes(@PathVariable Long id, Model model) {
        try {
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            List<br.com.arirang.plataforma.entity.AuditoriaTurma> historico = turmaService.buscarHistoricoAlteracoes(id);
            
            model.addAttribute("turma", turma);
            model.addAttribute("historico", historico);
            return "turma-historico";
        } catch (Exception e) {
            logger.error("Erro ao carregar histórico para turma ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar histórico: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/boletim/padrao/{id}")
    public String configurarPadraoBoletim(@PathVariable Long id, Model model) {
        try {
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            br.com.arirang.plataforma.dto.PadraoBoletimDTO padraoDTO = padraoBoletimService.buscarPadraoPorTurmaId(id)
                    .orElse(new br.com.arirang.plataforma.dto.PadraoBoletimDTO(
                            null, id, turma.nomeTurma(), null, null, null, null, null, null, null, null, null
                    ));
            
            model.addAttribute("turma", turma);
            model.addAttribute("padrao", padraoDTO);
            return "turma-padrao-boletim";
        } catch (Exception e) {
            logger.error("Erro ao carregar padrão de boletim para turma ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar padrão: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/boletim/padrao/{id}")
    public String salvarPadraoBoletim(@PathVariable Long id,
                                     @RequestParam(value = "descricaoExercicio", required = false) String descricaoExercicio,
                                     @RequestParam(value = "descricaoTrabalho", required = false) String descricaoTrabalho,
                                     @RequestParam(value = "descricaoAvaliacao", required = false) String descricaoAvaliacao,
                                     @RequestParam(value = "descricaoProducaoOral", required = false) String descricaoProducaoOral,
                                     @RequestParam(value = "descricaoProducaoEscrita", required = false) String descricaoProducaoEscrita,
                                     @RequestParam(value = "descricaoCompreensaoOral", required = false) String descricaoCompreensaoOral,
                                     @RequestParam(value = "descricaoCompreensaoEscrita", required = false) String descricaoCompreensaoEscrita,
                                     @RequestParam(value = "descricaoProvaFinal", required = false) String descricaoProvaFinal,
                                     @RequestParam(value = "descricaoPresenca", required = false) String descricaoPresenca,
                                     RedirectAttributes redirectAttributes) {
        try {
            TurmaDTO turma = turmaService.buscarTurmaPorIdAsDTO(id)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + id));
            
            br.com.arirang.plataforma.dto.PadraoBoletimDTO padraoDTO = new br.com.arirang.plataforma.dto.PadraoBoletimDTO(
                    null, id, turma.nomeTurma(),
                    descricaoExercicio, descricaoTrabalho, descricaoAvaliacao,
                    descricaoProducaoOral, descricaoProducaoEscrita,
                    descricaoCompreensaoOral, descricaoCompreensaoEscrita,
                    descricaoProvaFinal, descricaoPresenca
            );
            
            padraoBoletimService.salvarOuAtualizarPadrao(id, padraoDTO);
            redirectAttributes.addFlashAttribute("success", "Padrão de boletim salvo com sucesso");
            return "redirect:/turmas/boletim/padrao/" + id;
        } catch (Exception e) {
            logger.error("Erro ao salvar padrão de boletim para turma ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar padrão: " + e.getMessage());
            return "redirect:/turmas/boletim/padrao/" + id;
        }
    }
    
}