package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Classe che implementa l'interfaccia UserDetails
 * Il nostro username è la mail
 */

@Data
@Document(collection = "users")
@NoArgsConstructor
public class UserEntity implements UserDetails {

    boolean isAccountNonExpired;
    boolean isAccountNonLocked;
    boolean isCredentialsNonExpired;
    boolean isEnabled;
    @Id
    private ObjectId id;
    private String username;
    private String name;
    private String surname;
    private String password;
    private Set<RoleEntity> roleList;
    private Set<String> childrenList; //puoi prenotare solo per i tuoi figli
    private Date creationDate;


    public UserEntity(UserDTO userDTO, HashSet<RoleEntity> userRoles, PasswordEncoder passwordEncoder) {
        username = userDTO.getEmail();
        String[] mail_splitted = username.split("@")[0].split("\\.");
        name = StringUtils.capitalize(mail_splitted[0]);
        surname = StringUtils.capitalize(mail_splitted[1]);
        password = passwordEncoder.encode(userDTO.getPassword());
        isAccountNonExpired = true;
        isAccountNonLocked = true;
        isCredentialsNonExpired = true;
        isEnabled = false;
        roleList = new HashSet<>();
        roleList.addAll(userRoles);
        childrenList = new HashSet<>();
        creationDate = MongoTimeService.getNow();
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
        creationDate = MongoTimeService.getNow();
    }

    /**
     * Utilizzabile solo da Admin
     *
     * @param userInsertResource
     * @param userRoles
     * @param passwordEncoder
     */
    public UserEntity(UserInsertResource userInsertResource, HashSet<RoleEntity> userRoles, PasswordEncoder passwordEncoder) {
        username = userInsertResource.getUserId();
        name = userInsertResource.getName();
        surname = userInsertResource.getSurname();
        childrenList = userInsertResource.getChildIdList();
        // Password di default
        password = passwordEncoder.encode(this.surname + this.name + this.username.length());

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
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getId())));

        return grantedAuthorities;
    }

    public void addChild(String cf) {
        childrenList.add(cf);
    }


}
