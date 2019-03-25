package it.polito.mmap.esercizio1.customValidators;


import it.polito.mmap.esercizio1.viewModels.UserVM;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.ConcurrentHashMap;

public class DuplicateEmailValidator implements ConstraintValidator<DuplicateEmail, String> {

    @Autowired
    ConcurrentHashMap<String, UserVM> users;

    @Override
    public void initialize(DuplicateEmail email) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        return !users.containsKey(email);
    }

}