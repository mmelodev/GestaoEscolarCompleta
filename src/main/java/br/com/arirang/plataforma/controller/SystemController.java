package br.com.arirang.plataforma.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/system")
public class SystemController {

    @Value("${spring.application.version:1.0.0}")
    private String version;

    @GetMapping("/status")
    public String systemStatus() {
        return "system-status";
    }
}

@RestController
@RequestMapping("/api/system")
class SystemApiController {

    @Value("${spring.application.version:1.0.0}")
    private String version;

    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", version);
        info.put("buildDate", LocalDateTime.now());
        info.put("environment", "homologacao");
        info.put("updateAvailable", false); // Seria verificado contra GitHub API
        info.put("updateUrl", "https://github.com/SEU_USUARIO/SEU_REPOSITORIO/releases/latest");
        return info;
    }

    @GetMapping("/check-updates")
    public Map<String, Object> checkUpdates() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentVersion", version);
        result.put("latestVersion", "1.0.1"); // Seria obtido da GitHub API
        result.put("updateAvailable", true);
        result.put("downloadUrl", "https://github.com/SEU_USUARIO/SEU_REPOSITORIO/releases/latest/download/plataforma-0.0.1-SNAPSHOT.jar");
        result.put("instructions", "Baixe o novo JAR e substitua o atual");
        return result;
    }
}
