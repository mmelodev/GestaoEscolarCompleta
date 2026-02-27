package br.com.arirang.plataforma.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthWebController {

    private static final Logger logger = LoggerFactory.getLogger(AuthWebController.class);

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        try {
            // Se o usuário já estiver autenticado, redirecionar para home
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser")) {
                return "redirect:/home";
            }
            
            if (error != null) {
                model.addAttribute("error", "Usuário ou senha inválidos!");
            }
            
            if (logout != null) {
                model.addAttribute("message", "Logout realizado com sucesso!");
            }
            
            return "login";
        } catch (Exception e) {
            logger.error("Erro ao carregar página de login: ", e);
            // Retornar página de login mesmo em caso de erro para evitar 502
            model.addAttribute("error", "Erro ao carregar a página. Por favor, tente novamente.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        // O logout é gerenciado pelo Spring Security via SecurityConfig
        return "redirect:/login?logout=true";
    }
}
