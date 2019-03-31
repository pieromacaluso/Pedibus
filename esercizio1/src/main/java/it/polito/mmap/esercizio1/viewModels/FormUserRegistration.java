package it.polito.mmap.esercizio1.viewModels;

import it.polito.mmap.esercizio1.controllers.HomeController;
import it.polito.mmap.esercizio1.customValidators.EmailIsPresent;
import it.polito.mmap.esercizio1.customValidators.FieldsValueMatch;
import lombok.Data;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


//CustomValidator dichiarato nel relativo package, controlla che le pw siano uguali
//A differenza degli altri Validator si riferisce all'oggetto e non a un campo. Vedi posizione
@FieldsValueMatch(
        field = "pass",
        fieldMatch = "pass1",
        message = "Passwords do not match!"
)
@Data
public class FormUserRegistration {

    @Size(min = 3,max = 50, message = "Insert a valid name")
    private String first;

    @Size(min = 3,max = 50, message = "Insert a valid surname")
    private String last;

    //@EmailIsPresent è un Custom validator definito nel relativo package, controlla che la mail non sia già in uso
    @EmailIsPresent(expectedResult = false, message = "Mail not available")
    @Email
    @Size(min = 7,max = 25, message = "Mail length should be between 7 and 255")
    private String email;

    //si può anche usare "{Size.FormUserRegistration.pass}" con questa chiave settata in messages.properties
    @Size(min = 3,max = 64, message = "Password length should be between 3 and 64")
    private String pass;

    @Size(min = 3,max = 64, message = "Password length should be between 3 and 64")
    private String pass1;

    @AssertTrue
    boolean privacy;


}
