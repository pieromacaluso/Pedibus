package it.polito.mmap.esercizio1.viewModels;

import it.polito.mmap.esercizio1.controllers.HomeController;
import it.polito.mmap.esercizio1.customValidators.EmailIsPresent;
import it.polito.mmap.esercizio1.customValidators.FieldsValueMatch;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Data
public class FormUserLogin {

    @Email
    @Size(min = 7, max = 255)
    private String email;

    @Size(min = 3, max = 64)
    private String pass;

}
