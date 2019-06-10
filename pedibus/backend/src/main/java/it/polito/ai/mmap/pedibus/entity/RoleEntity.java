package it.polito.ai.mmap.pedibus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * I ruoli registrati nel nostro db al momento sono 3: ROLE_ADMIN, ROLE_USER e ROLE_SYSTEM-ADMIN
 */
@Data
@Builder
@Document(collection = "roles")
public class RoleEntity {
    @Id
    @JsonIgnore
    private String id;
    private String role;
}


