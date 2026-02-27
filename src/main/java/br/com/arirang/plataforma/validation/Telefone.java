package br.com.arirang.plataforma.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TelefoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Telefone {
    String message() default "Telefone inválido. Use o formato: (11) 99999-9999 ou (11) 3333-4444";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
