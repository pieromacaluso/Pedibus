package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.services.MongoZonedDateTime;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.UserService;
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

import javax.swing.text.html.parser.Entity;
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
    private Set<RoleEntity> roleList;
    private Set<ChildEntity> childrenList; //puoi prenotare solo per i tuoi figli
    private Date creationDate;
    private String userId;                  //in teoria non serviva ma è usato nell'UserService


    public UserEntity() {
    }

    public UserEntity(UserDTO userDTO, HashSet<RoleEntity> userRoles, PasswordEncoder passwordEncoder,UserService userService) {
        username = userDTO.getEmail();
        password = passwordEncoder.encode(userDTO.getPassword());
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);
        creationDate = MongoZonedDateTime.getNow();

        childrenList=new HashSet<>();
        ChildEntity childEntity=new ChildEntity("figlio","1");          //todo realizzare correttamente
        userService.addChild(childEntity);
        childrenList.add(childEntity);

        ChildEntity childEntity2=new ChildEntity("figlio","2");
        userService.addChild(childEntity2);
        childrenList.add(childEntity2);
    }

    public UserEntity(String email, HashSet<RoleEntity> userRoles) {
        username = email;
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        roleList.forEach(role ->
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole())));

        return grantedAuthorities;
    }


}
