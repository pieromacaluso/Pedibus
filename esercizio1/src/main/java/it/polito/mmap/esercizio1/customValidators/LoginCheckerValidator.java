package it.polito.mmap.esercizio1.customValidators;

import it.polito.mmap.esercizio1.viewModels.FormUserLogin;
import it.polito.mmap.esercizio1.viewModels.FormUserRegistration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.ConcurrentHashMap;

public class LoginCheckerValidator implements ConstraintValidator<LoginChecker, FormUserLogin> {

    @Autowired
    ConcurrentHashMap<String, FormUserRegistration> users;

    @Override
    public void initialize(LoginChecker constraintAnnotation) {
    }

    @Override
    public boolean isValid(FormUserLogin user, ConstraintValidatorContext context) {

        if (users.containsKey(user.getEmail())) {
            FormUserRegistration data = users.get(user.getEmail());
            return data.getPass().equals(user.getPass());
        }
        return false;

    }
}
