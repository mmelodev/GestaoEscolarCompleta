package br.com.arirang.plataforma.config;

import br.com.arirang.plataforma.security.JwtAuthenticationFilter;
import br.com.arirang.plataforma.security.JwtUtil;
import br.com.arirang.plataforma.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Autowired
    public SecurityConfig(JwtUtil jwtUtil, UsuarioService usuarioService, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, usuarioService);
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configurar CSRF para funcionar com APIs REST (JWT) e páginas web
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // CORS - usar configuração do CorsConfig
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // CSRF: habilitado para web, desabilitado para APIs REST (usam JWT)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/api/**") // APIs REST não precisam de CSRF (usam JWT)
            )
            
            // Configuração de sessão - STATELESS para APIs, mas permitimos sessões para web
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Sessões para páginas web
            )
            
            // Autorização de requisições
            .authorizeHttpRequests(authz -> authz
                // ========== ENDPOINTS PÚBLICOS ==========
                // Autenticação
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/login", "/login/**", "/logout").permitAll()
                
                // Página inicial pública apenas para redirecionar ao login
                .requestMatchers("/").permitAll()
                
                // Assets estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/webjars/**").permitAll()
                
                // Logos personalizados dos usuários
                .requestMatchers("/configuracao/logo/**").permitAll()
                
                // Swagger/OpenAPI (apenas em dev)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Spring Boot Actuator Health Checks (público para Railway/Cloudflare)
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                
                // Health check customizado (diagnóstico)
                .requestMatchers("/health", "/health/**").permitAll()
                
                // Página de erro
                .requestMatchers("/error").permitAll()
                
                // ========== ENDPOINTS PROTEGIDOS ==========
                // APIs REST - requerem autenticação JWT
                .requestMatchers("/api/**").authenticated()
                
                // Páginas administrativas - requerem role ADMIN ou USER
                .requestMatchers("/alunos/**", "/professores/**", "/turmas/**", 
                                "/contratos/**", "/financeiro/**", "/funcionarios/**",
                                "/boletim/**", "/cracha/**").hasAnyRole("ADMIN", "USER")
                
                // Páginas de cadastro - qualquer usuário autenticado
                .requestMatchers("/cadastro/**", "/perfil/**").authenticated()
                
                // Qualquer outra requisição requer autenticação
                .anyRequest().authenticated()
            )
            
            // Formulário de login customizado
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            )
            
            // Adicionar filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
