package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.validator.FieldsValueMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@EqualsAndHashCode()
@FieldsValueMatch(
        field = "password",
        fieldMatch = "passMatch",
        message = "Passwords do not match!"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserPassDTO {
    @Pattern(regexp = "^((?=.*[0-9])|(?=.*[@#$%^&+!=]))((?=.*[a-z])|(?=.*[A-Z]))(?=\\S+$).{8,}$", flags = Pattern.Flag.UNICODE_CASE)
    @NotEmpty
    @Size(min = 3, max = 64)
    private String password;

    @NotEmpty
    @Size(min = 3, max = 64)
    private String passMatch;
}
