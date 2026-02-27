package br.com.arirang.plataforma.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");

    @Override
    public void initialize(CPF constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return true; // CPF é opcional, validação @NotNull deve ser usada separadamente
        }

        // Remove caracteres não numéricos e espaços
        String cpfLimpo = cpf.replaceAll("\\D", "").trim();
        
        // Se após limpar não tem nada, considera válido (campo opcional)
        if (cpfLimpo.isEmpty()) {
            return true;
        }
        
        // Verifica se tem exatamente 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }
        
        // Verifica se tem 11 dígitos usando o pattern
        if (!CPF_PATTERN.matcher(cpfLimpo).matches()) {
            return false;
        }

        // Verifica se não são todos os dígitos iguais (ex: 111.111.111-11)
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Validação do algoritmo do CPF
        return isValidCPFAlgorithm(cpfLimpo);
    }

    private boolean isValidCPFAlgorithm(String cpf) {
        try {
            // Calcula o primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int resto = soma % 11;
            int primeiroDigito = (resto < 2) ? 0 : 11 - resto;

            // Calcula o segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = soma % 11;
            int segundoDigito = (resto < 2) ? 0 : 11 - resto;

            // Verifica se os dígitos calculados coincidem com os informados
            return primeiroDigito == Character.getNumericValue(cpf.charAt(9)) &&
                   segundoDigito == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }
}
