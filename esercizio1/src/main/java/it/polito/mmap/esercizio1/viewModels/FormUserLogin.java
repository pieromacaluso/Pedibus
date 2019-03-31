package it.polito.mmap.esercizio1.viewModels;

import it.polito.mmap.esercizio1.controllers.HomeController;
import it.polito.mmap.esercizio1.customValidators.EmailIsPresent;
import it.polito.mmap.esercizio1.customValidators.FieldsValueMatch;
import it.polito.mmap.esercizio1.customValidators.LoginChecker;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Data
@LoginChecker
public class FormUserLogin {

    @EmailIsPresent(expectedResult = true, message = "Mail not valid")
    @Email
    @Size(min = 7, max = 255, message = "Mail length should be between 7 and 255")
    private String email;

    @Size(min = 3, max = 64, message = "Password length should be between 3 and 64")
    private String pass;

}
