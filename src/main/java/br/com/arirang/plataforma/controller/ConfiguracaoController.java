package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.ConfiguracaoUsuarioDTO;
import br.com.arirang.plataforma.service.ConfiguracaoUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/configuracao")
@PreAuthorize("isAuthenticated()")
public class ConfiguracaoController {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracaoController.class);

    @Autowired
    private ConfiguracaoUsuarioService configuracaoUsuarioService;

    /**
     * Exibe a página de configurações de personalização
     */
    @GetMapping
    public String exibirConfiguracao(Model model) {
        try {
            ConfiguracaoUsuarioDTO config = configuracaoUsuarioService.buscarOuCriarConfiguracao();
            model.addAttribute("configuracao", config);
            return "configuracao";
        } catch (Exception e) {
            logger.error("Erro ao buscar configuração: ", e);
            model.addAttribute("error", "Erro ao carregar configurações");
            return "configuracao";
        }
    }

    /**
     * Salva as configurações de personalização
     */
    @PostMapping("/salvar")
    public String salvarConfiguracao(
            @ModelAttribute ConfiguracaoUsuarioDTO configuracao,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes) {
        try {
            configuracaoUsuarioService.salvarConfiguracao(configuracao, logoFile);
            redirectAttributes.addFlashAttribute("success", "Configurações salvas com sucesso!");
            logger.info("Configurações salvas com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao salvar configuração: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar configurações: " + e.getMessage());
        }
        return "redirect:/configuracao";
    }

    /**
     * Remove o logo personalizado
     */
    @PostMapping("/remover-logo")
    public String removerLogo(RedirectAttributes redirectAttributes) {
        try {
            configuracaoUsuarioService.removerLogo();
            redirectAttributes.addFlashAttribute("success", "Logo removido com sucesso!");
            logger.info("Logo removido com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao remover logo: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao remover logo: " + e.getMessage());
        }
        return "redirect:/configuracao";
    }

    /**
     * Reseta as configurações para valores padrão
     */
    @PostMapping("/resetar")
    public String resetarConfiguracao(RedirectAttributes redirectAttributes) {
        try {
            configuracaoUsuarioService.resetarConfiguracao();
            redirectAttributes.addFlashAttribute("success", "Configurações resetadas para valores padrão!");
            logger.info("Configurações resetadas");
        } catch (Exception e) {
            logger.error("Erro ao resetar configuração: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao resetar configurações: " + e.getMessage());
        }
        return "redirect:/configuracao";
    }

    /**
     * API REST para buscar configuração (para uso em JavaScript)
     */
    @GetMapping("/api")
    @ResponseBody
    public ConfiguracaoUsuarioDTO buscarConfiguracaoAPI() {
        return configuracaoUsuarioService.buscarOuCriarConfiguracao();
    }

    /**
     * Serve o logo personalizado do usuário
     */
    @GetMapping("/logo/{filename:.+}")
    public ResponseEntity<Resource> obterLogo(@PathVariable String filename) {
        try {
            Path logoDir = Paths.get("uploads/logos-usuarios");
            Path logoPath = logoDir.resolve(filename).normalize();
            
            // Verificar se o arquivo está dentro do diretório permitido (prevenir path traversal)
            if (!logoPath.startsWith(logoDir.normalize())) {
                return ResponseEntity.notFound().build();
            }
            
            if (!Files.exists(logoPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(logoPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determinar content type
            String contentType = "image/png";
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF_VALUE;
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            logger.error("Erro ao servir logo: ", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao servir logo: ", e);
            return ResponseEntity.notFound().build();
        }
    }
}
