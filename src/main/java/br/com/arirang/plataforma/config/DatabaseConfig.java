package br.com.arirang.plataforma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.arirang.plataforma.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuração básica do JPA
}
