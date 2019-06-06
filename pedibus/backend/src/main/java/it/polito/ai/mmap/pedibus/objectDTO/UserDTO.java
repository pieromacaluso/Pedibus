package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.validator.FieldsValueMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
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
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends ResourceSupport {
    @Email
    @Size(min = 7, max = 64)
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", flags = Pattern.Flag.UNICODE_CASE)
    @Size(min = 3, max = 64)
    private String password;

    @Size(min = 3, max = 64)
    private String passMatch;
}


