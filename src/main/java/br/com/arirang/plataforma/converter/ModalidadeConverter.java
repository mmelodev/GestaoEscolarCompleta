package br.com.arirang.plataforma.converter;

import br.com.arirang.plataforma.enums.Modalidade;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class ModalidadeConverter implements AttributeConverter<Modalidade, String> {

    private static final Logger logger = LoggerFactory.getLogger(ModalidadeConverter.class);

    @Override
    public String convertToDatabaseColumn(Modalidade modalidade) {
        if (modalidade == null) {
            return null;
        }
        return modalidade.name();
    }

    @Override
    public Modalidade convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Modalidade.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Log do erro mas não quebra a aplicação
            logger.warn("Valor inválido de Modalidade encontrado no banco: '{}'. Retornando null. Valores válidos: {}", 
                    dbData, java.util.Arrays.toString(Modalidade.values()));
            
            // Retornar null em vez de lançar exceção
            // Isso permite que a aplicação continue funcionando mesmo com dados inconsistentes
            return null;
        }
    }
}

