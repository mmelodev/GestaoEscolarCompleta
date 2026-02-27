package br.com.arirang.plataforma.config;

import br.com.arirang.plataforma.dto.ConfiguracaoUsuarioDTO;
import br.com.arirang.plataforma.service.ConfiguracaoUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor para aplicar configurações de tema do usuário em todas as páginas
 */
@Component
public class ThemeInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ThemeInterceptor.class);

    @Autowired
    private ConfiguracaoUsuarioService configuracaoUsuarioService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) throws Exception {
        
        // Apenas para requisições que retornam views (não APIs)
        if (modelAndView != null) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                // Apenas se usuário estiver autenticado
                if (authentication != null && authentication.isAuthenticated() 
                    && !authentication.getName().equals("anonymousUser")) {
                    
                    try {
                        ConfiguracaoUsuarioDTO config = configuracaoUsuarioService.buscarOuCriarConfiguracao();
                        modelAndView.addObject("themeConfig", config);
                        logger.debug("Configuração de tema aplicada para usuário: {}", authentication.getName());
                    } catch (Exception e) {
                        // Ignorar erros silenciosamente
                        logger.debug("Não foi possível aplicar configuração de tema: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                // Não quebrar a requisição se houver erro ao buscar configuração
                logger.warn("Erro ao aplicar configuração de tema: {}", e.getMessage());
            }
        }
    }
}
