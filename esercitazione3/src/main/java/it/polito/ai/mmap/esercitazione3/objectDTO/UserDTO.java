package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.validator.FieldsValueMatch;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


/**
 * UserDTO che fa anche da resource e viene validato con le apposite annotazioni
 */
@EqualsAndHashCode(callSuper = true)
@FieldsValueMatch(
        field = "password",
        fieldMatch = "passMatch",
        message = "Passwords do not match!"
)
@Data
public class UserDTO extends ResourceSupport {
    @Email
    @Size(min = 7, max = 64)
    private String email;

    @Size(min = 3, max = 64)
    private String password;

    @Size(min = 3, max = 64)
    private String passMatch;
}
