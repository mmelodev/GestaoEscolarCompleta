package br.com.arirang.plataforma.converter;

import br.com.arirang.plataforma.enums.Turno;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToTurnoConverter implements Converter<String, Turno> {

    @Override
    public Turno convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        // Tentar converter pelo nome do enum primeiro (MATUTINO, VESPERTINO, etc.)
        try {
            return Turno.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Se falhar, tentar encontrar pela descrição
            for (Turno turno : Turno.values()) {
                if (turno.getDescricao().equalsIgnoreCase(source) || 
                    turno.name().equalsIgnoreCase(source)) {
                    return turno;
                }
            }
            // Se não encontrar, retornar null
            return null;
        }
    }
}

