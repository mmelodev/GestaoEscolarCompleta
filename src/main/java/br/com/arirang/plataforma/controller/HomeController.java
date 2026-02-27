package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.MensalidadeDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.MensalidadeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private MensalidadeService mensalidadeService;

    @GetMapping("/")
    public String landing() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                return "redirect:/home";
            }
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Erro ao processar landing page: ", e);
            return "redirect:/login";
        }
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                model.addAttribute("username", authentication.getName());
            }
            
            // Buscar aniversariantes do dia
            List<Aluno> aniversariantes = alunoService.buscarAniversariantesDoDia();
            // Filtrar apenas alunos com telefone cadastrado
            List<Aluno> aniversariantesComTelefone = aniversariantes.stream()
                    .filter(aluno -> aluno.getTelefone() != null && !aluno.getTelefone().trim().isEmpty())
                    .collect(Collectors.toList());
            
            model.addAttribute("aniversariantes", aniversariantesComTelefone);
            model.addAttribute("totalAniversariantes", aniversariantes.size());
            
            // Buscar alunos com pagamentos próximos (vencidas, hoje ou nos próximos 5 dias)
            List<MensalidadeDTO> pagamentosProximos = mensalidadeService.listarAlunosComPagamentosProximos();
            logger.debug("Pagamentos próximos encontrados: {}", pagamentosProximos.size());
            model.addAttribute("pagamentosProximos", pagamentosProximos);
            model.addAttribute("totalPagamentosProximos", pagamentosProximos != null ? pagamentosProximos.size() : 0);
            
            return "home";
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard: ", e);
            model.addAttribute("error", "Erro ao carregar o dashboard. Por favor, tente novamente.");
            return "home";
        }
    }

    @GetMapping("/funcionarios")
    public String funcionarios(Model model) {
        // Placeholder para mensagens (substituir por lógica real se houver controllers específicos)
        model.addAttribute("recepcaoMsg", "Funcionalidade em desenvolvimento");
        model.addAttribute("limpezaMsg", "Funcionalidade em desenvolvimento");
        return "funcionarios"; // Retorna o template funcionarios.html
    }
}