package it.polito.mmap.esercizio1.viewModels;

import it.polito.mmap.esercizio1.customValidators.FieldsValueMatch;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@FieldsValueMatch(
        field = "pass",
        fieldMatch = "pass1",
        message = "Passwords do not match!"
)
@Data
public class UserVM {

    @Size(min = 3,max = 50)
    private String first;

    @Size(min = 3,max = 50)
    private String last;

    @Email
    @Size(min = 7,max = 255)
    private String email;

    @Size(min = 3,max = 64)
    private String pass;

    @Size(min = 3,max = 64)
    private String pass1;

    //probabilmente non è la soluzione migliore, ma in thymeleaf non riesco ad accedere ai GlobalError che sono quegli errori derivanti da annotazioni di classe
    private String passMatchError;

    @AssertTrue //possiamo usare questa modificando il messaggio o definirne una nostra
    boolean privacy;

}
