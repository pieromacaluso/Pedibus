package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.services.MongoZonedDateTime;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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

    @Transient
    @Autowired
    private MongoZonedDateTime mongoZonedDateTime;

    @Id
    private ObjectId id;
    private String username;
    private String password;
    boolean isAccountNonExpired;
    boolean isAccountNonLocked;
    boolean isCredentialsNonExpired;
    boolean isEnabled;
    private ArrayList<RoleEntity> roleList;     //todo cambiare con set per evitare di aggiungere più volte stesso ruolo
    private Date creationDate;
    private String userId;                      //per stampare come campo json l'userId e non i vari campi di ObjectId


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
        creationDate = mongoZonedDateTime.getNow();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        roleList.forEach(role ->
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole())));

        return grantedAuthorities;
    }


}
