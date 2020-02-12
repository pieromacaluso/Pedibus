package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInsertResource {

    @Email
    @NotEmpty
    @Size(min = 7, max = 64)
    private String userId;
    @NotEmpty
    private String name;
    @NotEmpty
    private String surname;
    private List<String> roleIdList;
    private List<String> lineaIdList;
    private Set<String> childIdList;

    public UserInsertResource(LineaDTO lineaDTO) {
        userId = lineaDTO.getMasterMail();
        name = lineaDTO.getMasterName();
        surname = lineaDTO.getMasterSurname();
        roleIdList = new LinkedList<>();
        lineaIdList = Collections.singletonList(lineaDTO.getId());
        childIdList = Collections.emptySet();
    }

    public UserInsertResource(UserEntity userEntity, List<LineaEntity> listLine) {
        userId = userEntity.getUsername();
        name = userEntity.getName();
        surname = userEntity.getSurname();
        roleIdList = userEntity.getRoleList().stream().map(RoleEntity::getId).collect(Collectors.toList());
        childIdList = userEntity.getChildrenList();
        lineaIdList = listLine.stream().map(LineaEntity::getId).collect(Collectors.toList());

    }
}
