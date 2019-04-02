package it.polito.mmap.esercitazione1.validator;

import it.polito.mmap.esercitazione1.model.User;
import it.polito.mmap.esercitazione1.view.FormUserRegistration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.ConcurrentHashMap;

public class EmaiIlsPresentValidator implements ConstraintValidator<EmailIsPresent, String> {

    @Autowired
    ConcurrentHashMap<String, User> users;

    private boolean expectedResult;

    @Override
    public void initialize(EmailIsPresent constraintAnnotation) {
        this.expectedResult = constraintAnnotation.expectedResult();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        return users.containsKey(email) == expectedResult;
    }

}