package br.com.arirang.plataforma.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class TelefoneValidator implements ConstraintValidator<Telefone, String> {

    // Padrões para diferentes formatos de telefone brasileiro
    private static final Pattern TELEFONE_FIXO = Pattern.compile("\\d{10}"); // (11) 3333-4444
    private static final Pattern TELEFONE_CELULAR = Pattern.compile("\\d{11}"); // (11) 99999-9999
    private static final Pattern TELEFONE_INTERNACIONAL = Pattern.compile("\\+\\d{10,15}"); // +55 11 99999-9999

    @Override
    public void initialize(Telefone constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String telefone, ConstraintValidatorContext context) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return true; // Telefone é opcional
        }

        // Remove caracteres não numéricos (exceto + para internacional)
        String telefoneLimpo = telefone.replaceAll("[^\\d+]", "");
        
        // Verifica se é número internacional (começa com +)
        if (telefoneLimpo.startsWith("+")) {
            return TELEFONE_INTERNACIONAL.matcher(telefoneLimpo).matches();
        }
        
        // Verifica se é telefone fixo (10 dígitos) ou celular (11 dígitos)
        if (telefoneLimpo.length() == 10) {
            return TELEFONE_FIXO.matcher(telefoneLimpo).matches();
        } else if (telefoneLimpo.length() == 11) {
            return TELEFONE_CELULAR.matcher(telefoneLimpo).matches() && 
                   isValidCelular(telefoneLimpo);
        }
        
        return false;
    }

    private boolean isValidCelular(String celular) {
        // Celular deve começar com 9 no 3º dígito (DDD + 9 + 8 dígitos)
        return celular.charAt(2) == '9';
    }
}
