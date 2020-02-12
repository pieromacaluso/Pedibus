package it.polito.ai.mmap.pedibus.objectDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 * LoginUserDTO che fa anche da resource e viene validato con le apposite annotazioni
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO extends ResourceSupport {
    @Email
    @NotNull
    @Size(min = 7, max = 64)
    private String email;

    @Pattern(regexp = "^((?=.*[0-9])|(?=.*[@#$%^&+!=]))((?=.*[a-z])|(?=.*[A-Z]))(?=\\S+$).{8,}$", flags = Pattern.Flag.UNICODE_CASE)
    @NotNull
    @Size(min = 3, max = 64)
    private String password;
}


