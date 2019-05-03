package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Data
@Document(collection = "users")
public class UserEntity {
    @Id
    private ObjectId id;
    private String email;
    private String pass;
    private Boolean activationState;
    private ArrayList<RoleEntity> roles;


    public UserEntity() {}

    public UserEntity(UserDTO userDTO) {
        email = userDTO.getEmail();
        pass = userDTO.getPass();
        activationState = false;
    }
}
