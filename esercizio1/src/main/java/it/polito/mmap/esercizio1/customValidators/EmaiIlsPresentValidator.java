package it.polito.mmap.esercizio1.customValidators;


import it.polito.mmap.esercizio1.viewModels.UserVM;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.ConcurrentHashMap;

public class EmaiIlsPresentValidator implements ConstraintValidator<EmailIsPresent, String> {

    @Autowired
    ConcurrentHashMap<String, UserVM> users;

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