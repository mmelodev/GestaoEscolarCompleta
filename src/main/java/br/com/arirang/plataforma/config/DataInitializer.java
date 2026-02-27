package br.com.arirang.plataforma.config;

import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.default.test.password:}")
    private String testPassword;

    @Override
    public void run(String... args) throws Exception {
        // Criar usuário admin se não existir
        // Usa senha padrão "admin123" se não configurada via variável de ambiente
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@arirang.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNomeCompleto("Administrador do Sistema");
            admin.setRole(Usuario.Role.ADMIN);
            admin.setAtivo(true);

            usuarioRepository.save(admin);
            logger.info("✅ Usuário admin criado com sucesso!");
            logger.info("   Username: admin");
            logger.info("   Senha: {} (configure APP_DEFAULT_ADMIN_PASSWORD para personalizar)", 
                adminPassword.equals("admin123") ? "admin123 (padrão)" : "configurada");
            logger.warn("⚠️ IMPORTANTE: Altere a senha padrão após o primeiro login por questões de segurança!");
        } else {
            logger.info("ℹ️ Usuário admin já existe. Pulando criação.");
        }

        // Criar usuário de teste se não existir e senha estiver configurada
        if (usuarioRepository.findByUsername("teste").isEmpty() && !testPassword.isEmpty()) {
            Usuario teste = new Usuario();
            teste.setUsername("teste");
            teste.setEmail("teste@arirang.com");
            teste.setPassword(passwordEncoder.encode(testPassword));
            teste.setNomeCompleto("Usuário de Teste");
            teste.setRole(Usuario.Role.USER);
            teste.setAtivo(true);

            usuarioRepository.save(teste);
            logger.info("Usuário teste criado com sucesso");
        } else if (testPassword.isEmpty()) {
            logger.warn("Senha do usuário teste não configurada. Usuário teste não será criado automaticamente.");
        }
    }
}
