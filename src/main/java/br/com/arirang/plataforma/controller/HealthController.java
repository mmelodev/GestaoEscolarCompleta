package br.com.arirang.plataforma.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de Health Check para diagnóstico
 * Endpoints públicos para verificar status da aplicação (Railway/Cloudflare)
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired(required = false)
    private DataSource dataSource;

    @Value("${spring.application.name:plataforma}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Endpoint simples de health check (GET /health)
     * Retorna status básico da aplicação
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("status", "UP");
            health.put("application", applicationName);
            health.put("timestamp", LocalDateTime.now().toString());
            health.put("port", serverPort);
            
            // Verificar conexão com banco de dados
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(2); // timeout de 2 segundos
                    health.put("database", isValid ? "UP" : "DOWN");
                    if (!isValid) {
                        health.put("status", "DEGRADED");
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao verificar conexão com banco de dados: {}", e.getMessage());
                    health.put("database", "DOWN");
                    health.put("status", "DEGRADED");
                    health.put("databaseError", e.getMessage());
                }
            } else {
                health.put("database", "NOT_CONFIGURED");
            }
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Erro no health check: ", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Endpoint de readiness (GET /health/ready)
     * Verifica se a aplicação está pronta para receber tráfego
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        
        try {
            boolean ready = true;
            String status = "READY";
            
            // Verificar banco de dados
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(2);
                    readiness.put("database", isValid ? "READY" : "NOT_READY");
                    if (!isValid) {
                        ready = false;
                        status = "NOT_READY";
                    }
                } catch (Exception e) {
                    logger.warn("Banco de dados não está pronto: {}", e.getMessage());
                    readiness.put("database", "NOT_READY");
                    readiness.put("databaseError", e.getMessage());
                    ready = false;
                    status = "NOT_READY";
                }
            }
            
            readiness.put("status", status);
            readiness.put("ready", ready);
            readiness.put("timestamp", LocalDateTime.now().toString());
            
            return ready ? ResponseEntity.ok(readiness) : ResponseEntity.status(503).body(readiness);
            
        } catch (Exception e) {
            logger.error("Erro no readiness check: ", e);
            readiness.put("status", "NOT_READY");
            readiness.put("ready", false);
            readiness.put("error", e.getMessage());
            return ResponseEntity.status(503).body(readiness);
        }
    }

    /**
     * Endpoint de liveness (GET /health/live)
     * Verifica se a aplicação está viva (não crashou)
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(liveness);
    }

}
