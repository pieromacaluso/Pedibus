package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import lombok.Data;
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
public class UserEntity implements UserDetails {
    @Id
    private ObjectId id;
    private String username;
    private String password;
    boolean isAccountNonExpired;
    boolean isAccountNonLocked;
    boolean isCredentialsNonExpired;
    boolean isEnabled;
    private ArrayList<RoleEntity> roleList;
    private Date creationDate;


    public UserEntity() {
    }

    public UserEntity(UserDTO userDTO, ArrayList<RoleEntity> userRoles, PasswordEncoder passwordEncoder) {
        username = userDTO.getEmail();
        password = passwordEncoder.encode(userDTO.getPassword());
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = userRoles;
       creationDate = new MongoZonedDateTime().getNow();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        roleList.forEach(role ->
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole())));

        return grantedAuthorities;
    }


}
