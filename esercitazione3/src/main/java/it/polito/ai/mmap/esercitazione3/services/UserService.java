package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.UserAlreadyPresentException;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.RoleRepository;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> check = userRepository.findByEmail(email);
        if (!check.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        UserEntity userEntity = check.get();
        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("user"));
        return new User(userEntity.getEmail(), userEntity.getPass(), authorities);
    }

    public void registerUser(UserDTO userDTO) {
        Optional<UserEntity> check = userRepository.findByEmail(userDTO.getEmail());
        if (check.isPresent()) {
            throw new UserAlreadyPresentException("User already registered");
        }
        RoleEntity userRole = roleRepository.findByRole("user");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPass(passwordEncoder.encode(userDTO.getPass()));
        userEntity.setActivationState(false);
        userEntity.setRoles(new ArrayList<>(Arrays.asList(userRole)));

        userRepository.save(userEntity);
    }

    public Boolean areCredentialsValid(UserDTO userDTO) {

        Optional<UserEntity> check = userRepository.findByEmail(userDTO.getEmail());
        UserEntity userEntity;
        if (!check.isPresent())
            return false;
        else {
            userEntity = check.get();
        }
        //if (userEntity.getEmail().equals(userDTO.getEmail()) && )
        return false;
    }


}
