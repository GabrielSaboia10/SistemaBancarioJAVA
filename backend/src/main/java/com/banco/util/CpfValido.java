package com.banco.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CpfValido.Validador.class)
public @interface CpfValido {

    String message() default "CPF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<CpfValido, String> {
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext ctx) {
            // Campo vazio é responsabilidade do @NotBlank
            return valor == null || valor.isBlank() || Cpf.isValido(valor);
        }
    }
}
