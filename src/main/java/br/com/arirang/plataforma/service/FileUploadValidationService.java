package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.exception.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Serviço para validação de uploads de arquivos
 */
@Service
public class FileUploadValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadValidationService.class);

    // Tipos MIME permitidos para imagens
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    // Extensões permitidas
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    @Value("${app.upload.max-file-size:5242880}") // 5MB padrão
    private long maxFileSize;

    @Value("${app.upload.allowed-extensions:.jpg,.jpeg,.png,.gif,.webp}")
    private String allowedExtensionsConfig;

    @Value("${app.upload.validate-image-content:true}")
    private boolean validateImageContent;

    @Value("${app.upload.max-image-width:4096}")
    private int maxImageWidth;

    @Value("${app.upload.max-image-height:4096}")
    private int maxImageHeight;

    /**
     * Valida um arquivo de upload
     * 
     * @param file Arquivo a ser validado
     * @throws FileUploadException Se o arquivo não passar na validação
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Arquivo não fornecido ou está vazio");
        }

        // Validar nome do arquivo
        validateFileName(file.getOriginalFilename());

        // Validar tamanho
        validateFileSize(file.getSize());

        // Validar extensão
        String extension = getFileExtension(file.getOriginalFilename());
        validateFileExtension(extension);

        // Validar tipo MIME
        validateMimeType(file.getContentType(), extension);

        // Validar conteúdo da imagem (se configurado)
        if (validateImageContent) {
            validateImageContent(file);
        }
    }

    /**
     * Valida o nome do arquivo para prevenir path traversal e caracteres perigosos
     */
    private void validateFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new FileUploadException("Nome do arquivo não pode ser vazio");
        }

        // Normalizar o caminho para prevenir path traversal
        Path normalized = Paths.get(filename).normalize();
        if (!normalized.toString().equals(filename.replace("\\", "/"))) {
            throw new FileUploadException("Nome do arquivo contém caracteres inválidos ou tentativa de path traversal");
        }

        // Verificar caracteres perigosos
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new FileUploadException("Nome do arquivo contém caracteres inválidos");
        }

        // Limitar tamanho do nome (250 caracteres)
        if (filename.length() > 250) {
            throw new FileUploadException("Nome do arquivo muito longo (máximo 250 caracteres)");
        }
    }

    /**
     * Valida o tamanho do arquivo
     */
    private void validateFileSize(long size) {
        if (size <= 0) {
            throw new FileUploadException("Arquivo está vazio");
        }

        if (size > maxFileSize) {
            long maxSizeMB = maxFileSize / (1024 * 1024);
            throw new FileUploadException(
                String.format("Arquivo muito grande. Tamanho máximo permitido: %d MB", maxSizeMB)
            );
        }
    }

    /**
     * Valida a extensão do arquivo
     */
    private void validateFileExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            throw new FileUploadException("Arquivo sem extensão");
        }

        String lowerExtension = extension.toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(lowerExtension)) {
            throw new FileUploadException(
                String.format("Extensão de arquivo não permitida. Permitidas: %s", 
                    String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }

    /**
     * Valida o tipo MIME do arquivo
     */
    private void validateMimeType(String mimeType, String extension) {
        if (mimeType == null || mimeType.isEmpty()) {
            logger.warn("Tipo MIME não fornecido para arquivo com extensão: {}", extension);
            // Não bloqueamos se o MIME type não for fornecido, mas logamos
            return;
        }

        String lowerMimeType = mimeType.toLowerCase();
        if (!ALLOWED_IMAGE_MIME_TYPES.contains(lowerMimeType)) {
            throw new FileUploadException(
                String.format("Tipo de arquivo não permitido. Tipo fornecido: %s", mimeType)
            );
        }

        // Verificar se o MIME type corresponde à extensão
        if (!isMimeTypeMatchingExtension(lowerMimeType, extension)) {
            logger.warn("Tipo MIME '{}' não corresponde à extensão '{}'", mimeType, extension);
            // Não bloqueamos, mas logamos como aviso
        }
    }

    /**
     * Verifica se o MIME type corresponde à extensão
     */
    private boolean isMimeTypeMatchingExtension(String mimeType, String extension) {
        String lowerExt = extension.toLowerCase();
        
        if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
            return lowerExt.equals(".jpg") || lowerExt.equals(".jpeg");
        }
        if (mimeType.equals("image/png")) {
            return lowerExt.equals(".png");
        }
        if (mimeType.equals("image/gif")) {
            return lowerExt.equals(".gif");
        }
        if (mimeType.equals("image/webp")) {
            return lowerExt.equals(".webp");
        }
        return false;
    }

    /**
     * Valida o conteúdo da imagem verificando se é realmente uma imagem válida
     */
    private void validateImageContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            
            if (image == null) {
                throw new FileUploadException("Arquivo não é uma imagem válida");
            }

            // Validar dimensões
            int width = image.getWidth();
            int height = image.getHeight();

            if (width <= 0 || height <= 0) {
                throw new FileUploadException("Imagem com dimensões inválidas");
            }

            if (width > maxImageWidth || height > maxImageHeight) {
                throw new FileUploadException(
                    String.format("Imagem muito grande. Dimensões máximas: %dx%d pixels", 
                        maxImageWidth, maxImageHeight)
                );
            }

            // Verificar se a imagem tem conteúdo válido (não está corrompida)
            if (width == 0 || height == 0) {
                throw new FileUploadException("Imagem está corrompida ou vazia");
            }

            logger.debug("Imagem validada: {}x{} pixels, tamanho: {} bytes", 
                width, height, file.getSize());

        } catch (IOException e) {
            logger.error("Erro ao validar conteúdo da imagem: {}", e.getMessage());
            throw new FileUploadException("Erro ao processar imagem: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém a extensão do arquivo
     */
    public String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDot).toLowerCase();
    }

    /**
     * Gera um nome de arquivo seguro baseado no ID e extensão
     */
    public String generateSafeFilename(Long id, String extension) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        String safeExtension = getFileExtension(extension);
        if (safeExtension.isEmpty()) {
            safeExtension = ".jpg"; // Extensão padrão
        }
        
        return id + "_foto" + safeExtension;
    }

    /**
     * Valida e normaliza um caminho de diretório de upload
     */
    public Path validateAndNormalizeUploadPath(Path uploadDir, String filename) {
        Path targetFile = uploadDir.resolve(filename).normalize();
        Path normalizedDir = uploadDir.normalize();
        
        // Verificar se o arquivo está dentro do diretório de upload (prevenir path traversal)
        if (!targetFile.startsWith(normalizedDir)) {
            throw new FileUploadException("Tentativa de acesso a caminho não autorizado");
        }
        
        return targetFile;
    }
}

