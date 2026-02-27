package br.com.arirang.plataforma.converter;

import br.com.arirang.plataforma.enums.Modalidade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToModalidadeConverter implements Converter<String, Modalidade> {

    @Override
    public Modalidade convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        // Tentar converter pelo nome do enum primeiro (REGULAR, INTENSIVO, etc.)
        try {
            return Modalidade.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Se falhar, tentar encontrar pela descrição
            for (Modalidade modalidade : Modalidade.values()) {
                if (modalidade.getDescricao().equalsIgnoreCase(source) || 
                    modalidade.name().equalsIgnoreCase(source)) {
                    return modalidade;
                }
            }
            // Se não encontrar, retornar null
            return null;
        }
    }
}

