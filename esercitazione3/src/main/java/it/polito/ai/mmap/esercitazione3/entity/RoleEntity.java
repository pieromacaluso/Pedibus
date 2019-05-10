package it.polito.ai.mmap.esercitazione3.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * I ruoli registrati nel nostro db al momento sono 2: ROLE_ADMIN e ROLE_USER
 */
@Data
@Document(collection = "roles")
public class RoleEntity {
    @Id
    private String id;
    private String role;
}