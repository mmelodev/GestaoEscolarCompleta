package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.LoginRequest;
import br.com.arirang.plataforma.dto.LoginResponse;
import br.com.arirang.plataforma.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = usuarioService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro no login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro na autenticação: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Com JWT, o logout é feito no frontend removendo o token
        return ResponseEntity.ok("Logout realizado com sucesso");
    }
}
