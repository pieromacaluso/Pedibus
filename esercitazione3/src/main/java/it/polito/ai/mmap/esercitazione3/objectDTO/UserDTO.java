package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.validator.FieldsValueMatch;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@FieldsValueMatch(
        field = "pass",
        fieldMatch = "passMatch",
        message = "Passwords do not match!"
)
@Data
public class UserDTO extends ResourceSupport {
    @Email
    @Size(min = 7, max = 25)
    private String email;

    @Size(min = 3, max = 64)
    private String pass;

    @Size(min = 3, max = 64)
    private String passMatch;
}
