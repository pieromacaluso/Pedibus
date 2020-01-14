package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInsertResource {

    private String userId;
    private String name;
    private String surname;
    private List<String> roleIdList;
    private List<String> lineaIdList;

    public UserInsertResource(UserEntity userEntity) {
        userId = userEntity.getUsername();
        name = userEntity.getName();
        surname = userEntity.getSurname();
        roleIdList = userEntity.getRoleList().stream().map(RoleEntity::getId).collect(Collectors.toList());
    }
}
