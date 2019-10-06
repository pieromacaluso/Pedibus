package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 * Classe che implementa l'interfaccia UserDetails
 * Il nostro username è la mail
 *
 */

@Data
@Document(collection = "users")
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    private ObjectId id;
    private String username;
    private String password;
    boolean isAccountNonExpired;
    boolean isAccountNonLocked;
    boolean isCredentialsNonExpired;
    boolean isEnabled;
    private Set<RoleEntity> roleList;
    private Set<String> childrenList; //puoi prenotare solo per i tuoi figli
    private Date creationDate;


    public UserEntity(UserDTO userDTO, HashSet<RoleEntity> userRoles, PasswordEncoder passwordEncoder) {
        username = userDTO.getEmail();
        password = passwordEncoder.encode(userDTO.getPassword());
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);
        childrenList = new HashSet<>();
        creationDate = MongoZonedDateTime.getNow();
    }


    public UserEntity(UserDTO userDTO, HashSet<RoleEntity> userRoles, PasswordEncoder passwordEncoder, Set<String> childrenList) {
        username = userDTO.getEmail();
        password = passwordEncoder.encode(userDTO.getPassword());
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);
        this.childrenList = childrenList;
        creationDate = MongoZonedDateTime.getNow();
    }

    public UserEntity(String email, HashSet<RoleEntity> userRoles) {
        username = email;
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);
        childrenList = new HashSet<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        roleList.forEach(role ->
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getId())));

        return grantedAuthorities;
    }

    public void addChild(String cf){
        childrenList.add(cf);
    }


}
