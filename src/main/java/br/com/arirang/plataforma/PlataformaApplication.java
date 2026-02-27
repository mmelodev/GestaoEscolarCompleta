package br.com.arirang.plataforma;

import br.com.arirang.plataforma.config.RailwayDatabaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class PlataformaApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PlataformaApplication.class);
		// Registrar RailwayDatabaseConfig explicitamente para garantir execução
		app.addListeners(new RailwayDatabaseConfig());
		app.run(args);
	}

}


