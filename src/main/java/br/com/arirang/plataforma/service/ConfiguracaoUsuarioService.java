package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.ConfiguracaoUsuarioDTO;
import br.com.arirang.plataforma.entity.ConfiguracaoUsuario;
import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.exception.FileUploadException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.ConfiguracaoUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@Transactional
public class ConfiguracaoUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracaoUsuarioService.class);
    private static final Path LOGO_UPLOAD_DIR = Paths.get("uploads/logos-usuarios");

    @Autowired
    private ConfiguracaoUsuarioRepository configuracaoUsuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FileUploadValidationService fileUploadValidationService;

    /**
     * Busca ou cria configuração para o usuário atual
     */
    @Transactional(readOnly = true)
    public ConfiguracaoUsuarioDTO buscarOuCriarConfiguracao() {
        Usuario usuario = getUsuarioAtual();
        Optional<ConfiguracaoUsuario> configOpt = configuracaoUsuarioRepository.findByUsuario(usuario);
        
        if (configOpt.isPresent()) {
            return toDTO(configOpt.get());
        }
        
        // Retorna configuração padrão se não existir
        return ConfiguracaoUsuarioDTO.createDefault(usuario.getId());
    }

    /**
     * Salva ou atualiza configuração do usuário
     */
    @Transactional
    public ConfiguracaoUsuarioDTO salvarConfiguracao(ConfiguracaoUsuarioDTO dto, MultipartFile logoFile) {
        Usuario usuario = getUsuarioAtual();
        Optional<ConfiguracaoUsuario> configOpt = configuracaoUsuarioRepository.findByUsuario(usuario);
        
        ConfiguracaoUsuario config;
        if (configOpt.isPresent()) {
            config = configOpt.get();
            atualizarConfiguracao(config, dto);
        } else {
            config = criarConfiguracao(usuario, dto);
        }
        
        // Processar upload de logo se fornecido
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoUrl = salvarLogo(usuario.getId(), logoFile);
                config.setLogoUrl(logoUrl);
                logger.info("Logo salvo para usuário ID: {}", usuario.getId());
            } catch (Exception e) {
                logger.error("Erro ao salvar logo: ", e);
                throw new FileUploadException("Erro ao salvar logo: " + e.getMessage());
            }
        }
        
        ConfiguracaoUsuario salva = configuracaoUsuarioRepository.save(config);
        logger.info("Configuração salva para usuário ID: {}", usuario.getId());
        
        return toDTO(salva);
    }

    /**
     * Salva ou atualiza configuração do usuário (sobrecarga sem arquivo)
     */
    @Transactional
    public ConfiguracaoUsuarioDTO salvarConfiguracao(ConfiguracaoUsuarioDTO dto) {
        return salvarConfiguracao(dto, null);
    }

    /**
     * Remove o logo personalizado do usuário
     */
    @Transactional
    public void removerLogo() {
        Usuario usuario = getUsuarioAtual();
        Optional<ConfiguracaoUsuario> configOpt = configuracaoUsuarioRepository.findByUsuario(usuario);
        
        if (configOpt.isPresent()) {
            ConfiguracaoUsuario config = configOpt.get();
            String logoUrl = config.getLogoUrl();
            
            if (logoUrl != null && !logoUrl.isEmpty()) {
                // Remover arquivo físico se existir
                try {
                    if (logoUrl.startsWith("/configuracao/logo/")) {
                        String filename = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
                        Path logoPath = LOGO_UPLOAD_DIR.resolve(filename).normalize();
                        if (Files.exists(logoPath) && logoPath.startsWith(LOGO_UPLOAD_DIR.normalize())) {
                            Files.deleteIfExists(logoPath);
                            logger.info("Arquivo de logo removido: {}", filename);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao remover arquivo de logo: ", e);
                }
                
                // Limpar URL no banco
                config.setLogoUrl(null);
                configuracaoUsuarioRepository.save(config);
                logger.info("Logo removido para usuário ID: {}", usuario.getId());
            }
        }
    }

    /**
     * Salva o arquivo de logo e retorna a URL
     */
    private String salvarLogo(Long usuarioId, MultipartFile file) throws Exception {
        // Validar arquivo
        fileUploadValidationService.validateFile(file);
        
        // Criar diretório se não existir
        Files.createDirectories(LOGO_UPLOAD_DIR);
        
        // Gerar nome de arquivo seguro
        String extension = fileUploadValidationService.getFileExtension(file.getOriginalFilename());
        String filename = "logo_" + usuarioId + extension;
        
        // Validar e normalizar caminho (prevenir path traversal)
        Path target = fileUploadValidationService.validateAndNormalizeUploadPath(LOGO_UPLOAD_DIR, filename);
        
        // Remover logo anterior se existir
        try {
            Files.list(LOGO_UPLOAD_DIR)
                .filter(path -> path.getFileName().toString().startsWith("logo_" + usuarioId + "."))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        logger.warn("Erro ao remover logo anterior: ", e);
                    }
                });
        } catch (Exception e) {
            logger.warn("Erro ao limpar logos anteriores: ", e);
        }
        
        // Salvar arquivo
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar URL relativa
        return "/configuracao/logo/" + filename;
    }

    /**
     * Reseta configuração para valores padrão
     */
    @Transactional
    public ConfiguracaoUsuarioDTO resetarConfiguracao() {
        Usuario usuario = getUsuarioAtual();
        ConfiguracaoUsuarioDTO dtoPadrao = ConfiguracaoUsuarioDTO.createDefault(usuario.getId());
        return salvarConfiguracao(dtoPadrao);
    }

    /**
     * Busca configuração por ID do usuário (para uso interno)
     */
    @Transactional(readOnly = true)
    public Optional<ConfiguracaoUsuarioDTO> buscarPorUsuarioId(Long usuarioId) {
        return configuracaoUsuarioRepository.findByUsuarioId(usuarioId)
                .map(this::toDTO);
    }

    // Métodos privados auxiliares

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        
        String username = authentication.getName();
        return usuarioService.buscarPorUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
    }

    private ConfiguracaoUsuario criarConfiguracao(Usuario usuario, ConfiguracaoUsuarioDTO dto) {
        ConfiguracaoUsuario config = new ConfiguracaoUsuario();
        config.setUsuario(usuario);
        aplicarDTO(config, dto);
        return config;
    }

    private void atualizarConfiguracao(ConfiguracaoUsuario config, ConfiguracaoUsuarioDTO dto) {
        aplicarDTO(config, dto);
    }

    private void aplicarDTO(ConfiguracaoUsuario config, ConfiguracaoUsuarioDTO dto) {
        if (dto.corPrimaria() != null && !dto.corPrimaria().trim().isEmpty()) {
            config.setCorPrimaria(normalizarCor(dto.corPrimaria()));
        }
        if (dto.corSecundaria() != null && !dto.corSecundaria().trim().isEmpty()) {
            config.setCorSecundaria(normalizarCor(dto.corSecundaria()));
        }
        if (dto.corHeader() != null && !dto.corHeader().trim().isEmpty()) {
            config.setCorHeader(normalizarCor(dto.corHeader()));
        }
        if (dto.corTexto() != null && !dto.corTexto().trim().isEmpty()) {
            config.setCorTexto(normalizarCor(dto.corTexto()));
        }
        if (dto.corDestaque() != null && !dto.corDestaque().trim().isEmpty()) {
            config.setCorDestaque(normalizarCor(dto.corDestaque()));
        }
        if (dto.logoUrl() != null) {
            config.setLogoUrl(dto.logoUrl().length() > 500 ? dto.logoUrl().substring(0, 500) : dto.logoUrl());
        }
        if (dto.logoAlt() != null) {
            config.setLogoAlt(dto.logoAlt().length() > 100 ? dto.logoAlt().substring(0, 100) : dto.logoAlt());
        }
        if (dto.fonteTamanhoBase() != null) config.setFonteTamanhoBase(dto.fonteTamanhoBase());
        if (dto.bordaArredondada() != null) config.setBordaArredondada(dto.bordaArredondada());
        if (dto.temaEscuro() != null) config.setTemaEscuro(dto.temaEscuro());
    }

    /**
     * Normaliza uma cor para o formato hexadecimal (#RRGGBB) com máximo de 7 caracteres
     */
    private String normalizarCor(String cor) {
        if (cor == null || cor.trim().isEmpty()) {
            return null;
        }
        
        String corNormalizada = cor.trim();
        
        // Se já começa com #, pegar apenas os primeiros 7 caracteres
        if (corNormalizada.startsWith("#")) {
            corNormalizada = corNormalizada.substring(0, Math.min(corNormalizada.length(), 7));
            return corNormalizada;
        }
        
        // Se não começa com #, remover qualquer caractere não hexadecimal e pegar apenas os primeiros 6
        corNormalizada = corNormalizada.replaceAll("[^0-9A-Fa-f]", "");
        if (corNormalizada.length() > 6) {
            corNormalizada = corNormalizada.substring(0, 6);
        }
        
        // Se estiver vazia após limpeza, retornar null
        if (corNormalizada.isEmpty()) {
            return null;
        }
        
        return "#" + corNormalizada;
    }

    private ConfiguracaoUsuarioDTO toDTO(ConfiguracaoUsuario config) {
        return new ConfiguracaoUsuarioDTO(
            config.getId(),
            config.getUsuario().getId(),
            config.getCorPrimaria(),
            config.getCorSecundaria(),
            config.getCorHeader(),
            config.getCorTexto(),
            config.getCorDestaque(),
            config.getLogoUrl(),
            config.getLogoAlt(),
            config.getFonteTamanhoBase(),
            config.getBordaArredondada(),
            config.getTemaEscuro()
        );
    }
}
