
package it.polito.mmap.esercizio1.customValidators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = LoginCheckerValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginChecker {

    String message() default "Email and/or Password are incorrect!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}