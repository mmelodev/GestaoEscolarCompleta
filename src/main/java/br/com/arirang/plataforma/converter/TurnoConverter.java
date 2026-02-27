package br.com.arirang.plataforma.converter;

import br.com.arirang.plataforma.enums.Turno;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class TurnoConverter implements AttributeConverter<Turno, String> {

    private static final Logger logger = LoggerFactory.getLogger(TurnoConverter.class);

    @Override
    public String convertToDatabaseColumn(Turno turno) {
        if (turno == null) {
            return null;
        }
        return turno.name();
    }

    @Override
    public Turno convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Turno.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Tentar mapear valores antigos/inválidos para valores válidos
            String normalized = dbData.trim().toUpperCase()
                    .replace("Ã", "A")
                    .replace("Ç", "C");
            
            // Mapear valores comuns incorretos
            if (normalized.contains("MANHA") || normalized.contains("MANHÃ") || normalized.equals("MANHÃ")) {
                logger.warn("Valor '{}' mapeado para MATUTINO. Corrija no banco de dados.", dbData);
                return Turno.MATUTINO;
            }
            if (normalized.contains("TARDE") || normalized.contains("VESPERTINO")) {
                logger.warn("Valor '{}' mapeado para VESPERTINO. Corrija no banco de dados.", dbData);
                return Turno.VESPERTINO;
            }
            if (normalized.contains("NOITE") || normalized.contains("NOTURNO")) {
                logger.warn("Valor '{}' mapeado para NOTURNO. Corrija no banco de dados.", dbData);
                return Turno.NOTURNO;
            }
            
            // Se não conseguir mapear, logar e retornar null
            logger.warn("Valor inválido de Turno encontrado no banco: '{}'. Retornando null. Valores válidos: {}", 
                    dbData, java.util.Arrays.toString(Turno.values()));
            
            return null;
        }
    }
}

