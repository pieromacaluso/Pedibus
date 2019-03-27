/*
Definisco una nuova annotazione che mi permette di controllare che il campo email non sia già usato da un altro utente
source => https://www.baeldung.com/spring-mvc-custom-validator
 */

package it.polito.mmap.esercizio1.customValidators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = EmaiIlsPresentValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailIsPresent {

    String message() default "Mail already in use";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean expectedResult();

}