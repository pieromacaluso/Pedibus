package it.polito.mmap.esercizio1.viewModels;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

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

}
