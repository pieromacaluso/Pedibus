package it.polito.mmap.esercizio1.viewModels;

import it.polito.mmap.esercizio1.customValidators.EmailIsPresent;
import it.polito.mmap.esercizio1.customValidators.LoginChecker;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Data
@LoginChecker
public class FormUserLogin {

    @Email
    @Size(min = 7, max = 255)
    @EmailIsPresent(expectedResult = true, message = "Mail not valid")
    private String email;

    @Size(min = 3, max = 64)
    private String pass;

}
