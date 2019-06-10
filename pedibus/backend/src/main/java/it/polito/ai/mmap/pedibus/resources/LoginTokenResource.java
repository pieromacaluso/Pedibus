package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class LoginTokenResource {
    private String username;
    private String jwtToken;
    private Set<RoleEntity> roleList;
}
