package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.exception.FileUploadException;
import br.com.arirang.plataforma.repository.AlunoRepository;
import br.com.arirang.plataforma.service.FileUploadValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Controller
@RequestMapping("/cracha")
public class CrachaController {

    private static final Logger logger = LoggerFactory.getLogger(CrachaController.class);
    private static final Path UPLOAD_DIR = Paths.get("uploads/fotos-alunos");

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private FileUploadValidationService fileUploadValidationService;

    @GetMapping("/{alunoId}")
    public String crachaView(@PathVariable Long alunoId, Model model) throws Exception {
        Optional<Aluno> opt = alunoRepository.findByIdWithTurmasAndProf(alunoId);
        Aluno aluno = opt.orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        model.addAttribute("aluno", aluno);
        model.addAttribute("fotoUrl", "/cracha/foto/" + alunoId);
        return "cracha";
    }

    @PostMapping("/{alunoId}/upload")
    public String uploadFoto(
            @PathVariable Long alunoId,
            @RequestParam("foto") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            // Verificar se o aluno existe
            Optional<Aluno> alunoOpt = alunoRepository.findById(alunoId);
            if (alunoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Aluno não encontrado");
                return "redirect:/alunos/lista";
            }

            // Validar arquivo
            fileUploadValidationService.validateFile(file);

            // Criar diretório se não existir
            Files.createDirectories(UPLOAD_DIR);

            // Gerar nome de arquivo seguro
            String extension = fileUploadValidationService.getFileExtension(file.getOriginalFilename());
            String filename = fileUploadValidationService.generateSafeFilename(alunoId, extension);

            // Validar e normalizar caminho (prevenir path traversal)
            Path target = fileUploadValidationService.validateAndNormalizeUploadPath(UPLOAD_DIR, filename);

            // Salvar arquivo
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Foto do aluno {} enviada com sucesso: {}", alunoId, filename);
            redirectAttributes.addFlashAttribute("success", "Foto enviada com sucesso!");

        } catch (FileUploadException e) {
            logger.warn("Erro ao validar upload da foto do aluno {}: {}", alunoId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao fazer upload da foto do aluno {}: {}", alunoId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Erro ao fazer upload da foto: " + e.getMessage());
        }

        return "redirect:/cracha/" + alunoId;
    }

    @GetMapping("/foto/{alunoId}")
    public ResponseEntity<Resource> obterFoto(@PathVariable Long alunoId) throws MalformedURLException {
        String glob = alunoId + "_foto";
        try {
            // tenta jpg/png
            Path jpg = UPLOAD_DIR.resolve(glob + ".jpg");
            Path png = UPLOAD_DIR.resolve(glob + ".png");
            Path stored = Files.exists(jpg) ? jpg : (Files.exists(png) ? png : null);
            if (stored == null) return ResponseEntity.notFound().build();
            Resource resource = new UrlResource(stored.toUri());
            String contentType = stored.toString().endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + stored.getFileName())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}


