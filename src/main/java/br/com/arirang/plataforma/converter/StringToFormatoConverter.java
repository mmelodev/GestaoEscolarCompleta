package br.com.arirang.plataforma.converter;

import br.com.arirang.plataforma.enums.Formato;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFormatoConverter implements Converter<String, Formato> {

    @Override
    public Formato convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        // Tentar converter pelo nome do enum primeiro (PRESENCIAL, ONLINE, etc.)
        try {
            return Formato.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Se falhar, tentar encontrar pela descrição
            for (Formato formato : Formato.values()) {
                if (formato.getDescricao().equalsIgnoreCase(source) || 
                    formato.name().equalsIgnoreCase(source)) {
                    return formato;
                }
            }
            // Se não encontrar, retornar null
            return null;
        }
    }
}

