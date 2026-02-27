package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Professor;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.ProfessorService;
import br.com.arirang.plataforma.service.TurmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private static final Logger logger = LoggerFactory.getLogger(CadastroController.class);

    @Autowired
    private AlunoService alunoService;
    
    @Autowired
    private TurmaService turmaService;
    
    @Autowired
    private ProfessorService professorService;

    @GetMapping
    public String cadastro(@RequestParam(value = "search", required = false) String search, Model model) {
        List<CadastroItem> cadastros = new ArrayList<>();

        try {
            // Adicionar Alunos
            List<Aluno> alunos = alunoService.listarTodosAlunos();
            if (search != null && !search.trim().isEmpty()) {
                alunos = alunos.stream()
                        .filter(aluno -> aluno.getNomeCompleto().toLowerCase().contains(search.toLowerCase()) ||
                                       (aluno.getEmail() != null && aluno.getEmail().toLowerCase().contains(search.toLowerCase())) ||
                                       (aluno.getTelefone() != null && aluno.getTelefone().contains(search)))
                        .collect(Collectors.toList());
            }
            cadastros.addAll(alunos.stream()
                    .map(aluno -> new CadastroItem("Aluno", aluno.getId(), aluno.getNomeCompleto(), aluno.getEmail(), aluno.getTelefone()))
                    .collect(Collectors.toList()));

            // Adicionar Turmas (simplificado, sem e-mail)
            List<Turma> turmas = turmaService.listarTodasTurmas();
            if (search != null && !search.trim().isEmpty()) {
                turmas = turmas.stream()
                        .filter(turma -> turma.getNomeTurma().toLowerCase().contains(search.toLowerCase()))
                        .collect(Collectors.toList());
            }
            cadastros.addAll(turmas.stream()
                    .map(turma -> new CadastroItem("Turma", turma.getId(), turma.getNomeTurma(), null, null))
                    .collect(Collectors.toList()));

            // Adicionar Professores
            List<Professor> professores = professorService.listarTodosProfessores();
            if (search != null && !search.trim().isEmpty()) {
                professores = professores.stream()
                        .filter(professor -> professor.getNomeCompleto().toLowerCase().contains(search.toLowerCase()) ||
                                           (professor.getEmail() != null && professor.getEmail().toLowerCase().contains(search.toLowerCase())) ||
                                           (professor.getTelefone() != null && professor.getTelefone().contains(search)))
                        .collect(Collectors.toList());
            }
            cadastros.addAll(professores.stream()
                    .map(professor -> new CadastroItem("Professor", professor.getId(), professor.getNomeCompleto(), professor.getEmail(), professor.getTelefone()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Erro ao carregar cadastros: ", e);
            model.addAttribute("error", "Erro ao carregar a lista de cadastros: " + e.getMessage());
            return "error"; // Retorna um template de erro genérico
        }

        model.addAttribute("cadastros", cadastros);
        model.addAttribute("searchTerm", search);
        return "cadastro";
    }

    // Classe auxiliar para unificar os cadastros
    public static class CadastroItem {
        private String tipo;
        private Long id;
        private String nome;
        private String email;
        private String telefone;

        public CadastroItem(String tipo, Long id, String nome, String email, String telefone) {
            this.tipo = tipo;
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.telefone = telefone;
        }

        public String getTipo() { return tipo; }
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getTelefone() { return telefone; }
    }
}