package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.UpdateUsuarioPasswordRequest;
import br.com.arirang.plataforma.dto.UpdateUsuarioProfileRequest;
import br.com.arirang.plataforma.dto.UsuarioProfileDTO;
import br.com.arirang.plataforma.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);
    private static final String PROFILE_FORM_ATTR = "profileForm";
    private static final String PASSWORD_FORM_ATTR = "passwordForm";

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String exibirPerfil(Authentication authentication, Model model) {
        UsuarioProfileDTO perfil = (UsuarioProfileDTO) model.asMap()
                .getOrDefault("usuarioPerfilAtualizado", usuarioService.obterPerfil(authentication.getName()));

        boolean hasProfileErrors = model.containsAttribute("org.springframework.validation.BindingResult." + PROFILE_FORM_ATTR);
        if (!hasProfileErrors) {
            model.addAttribute(PROFILE_FORM_ATTR, new UpdateUsuarioProfileRequest(
                    perfil.nomeCompleto(),
                    perfil.email(),
                    perfil.telefone(),
                    perfil.bio(),
                    perfil.avatarUrl()
            ));
        }

        if (!model.containsAttribute(PASSWORD_FORM_ATTR)) {
            model.addAttribute(PASSWORD_FORM_ATTR, new UpdateUsuarioPasswordRequest("", "", ""));
        }

        model.addAttribute("usuarioPerfil", perfil);
        return "perfil";
    }

    @PostMapping("/dados")
    public String atualizarDados(
            Authentication authentication,
            @Valid @ModelAttribute(PROFILE_FORM_ATTR) UpdateUsuarioProfileRequest profileForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + PROFILE_FORM_ATTR, bindingResult);
            redirectAttributes.addFlashAttribute(PROFILE_FORM_ATTR, profileForm);
            return "redirect:/perfil";
        }

        try {
            UsuarioProfileDTO atualizado = usuarioService.atualizarPerfil(authentication.getName(), profileForm);
            redirectAttributes.addFlashAttribute("usuarioPerfilAtualizado", atualizado);
            redirectAttributes.addFlashAttribute(PROFILE_FORM_ATTR, new UpdateUsuarioProfileRequest(
                    atualizado.nomeCompleto(),
                    atualizado.email(),
                    atualizado.telefone(),
                    atualizado.bio(),
                    atualizado.avatarUrl()
            ));
            redirectAttributes.addFlashAttribute("perfilAtualizado", true);
        } catch (IllegalArgumentException ex) {
            logger.warn("Falha ao atualizar perfil: {}", ex.getMessage());
            bindingResult.rejectValue("email", "email.invalid", ex.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + PROFILE_FORM_ATTR, bindingResult);
            redirectAttributes.addFlashAttribute(PROFILE_FORM_ATTR, profileForm);
        }

        return "redirect:/perfil";
    }

    @PostMapping("/senha")
    public String atualizarSenha(
            Authentication authentication,
            @Valid @ModelAttribute(PASSWORD_FORM_ATTR) UpdateUsuarioPasswordRequest passwordForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + PASSWORD_FORM_ATTR, bindingResult);
            redirectAttributes.addFlashAttribute(PASSWORD_FORM_ATTR, passwordForm);
            return "redirect:/perfil";
        }

        try {
            usuarioService.alterarSenha(authentication.getName(), passwordForm);
            redirectAttributes.addFlashAttribute("senhaAtualizada", true);
        } catch (IllegalArgumentException ex) {
            logger.warn("Falha ao alterar senha: {}", ex.getMessage());
            bindingResult.reject("senha.invalida", ex.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + PASSWORD_FORM_ATTR, bindingResult);
            redirectAttributes.addFlashAttribute(PASSWORD_FORM_ATTR, passwordForm);
        }

        return "redirect:/perfil";
    }
}

