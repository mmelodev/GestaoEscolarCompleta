package br.com.arirang.plataforma.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CEPValidator implements ConstraintValidator<CEP, String> {

    private static final Pattern CEP_PATTERN = Pattern.compile("\\d{8}");

    @Override
    public void initialize(CEP constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String cep, ConstraintValidatorContext context) {
        if (cep == null || cep.trim().isEmpty()) {
            return true; // CEP é opcional
        }

        // Remove caracteres não numéricos
        String cepLimpo = cep.replaceAll("\\D", "");
        
        // Verifica se tem 8 dígitos
        if (!CEP_PATTERN.matcher(cepLimpo).matches()) {
            return false;
        }

        // Verifica se não são todos os dígitos iguais
        if (cepLimpo.matches("(\\d)\\1{7}")) {
            return false;
        }

        // Validações específicas do CEP brasileiro
        return isValidCEPRanges(cepLimpo);
    }

    private boolean isValidCEPRanges(String cep) {
        try {
            int cepNumero = Integer.parseInt(cep);
            
            // CEPs válidos no Brasil vão de 01000-000 a 99999-999
            // Excluindo alguns ranges específicos que não existem
            return cepNumero >= 1000000 && cepNumero <= 99999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
