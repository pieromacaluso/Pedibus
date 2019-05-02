package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@Document(collection = "users")
public class UserEntity {
    private String email;
    private String pass;
    private Boolean activationState;


    public UserEntity(UserDTO userDTO) {
        email = userDTO.getEmail();
        pass = userDTO.getPass();
        activationState = false;
    }
}
