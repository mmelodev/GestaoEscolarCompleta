package br.com.arirang.plataforma.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CEPValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CEP {
    String message() default "CEP inválido. Use o formato: 12345-678";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
