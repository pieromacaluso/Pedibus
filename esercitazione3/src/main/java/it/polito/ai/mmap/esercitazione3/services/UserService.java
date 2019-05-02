package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.UserAlreadyPresentException;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> check = userRepository.findByEmail(email);
        if(!check.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        UserEntity userEntity = check.get();
        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("user"));
        return new User(userEntity.getEmail(), userEntity.getPass(), authorities);
    }

    public void registerUser(UserDTO userDTO) {
        if (isUserPresent(userDTO.getEmail())) {
            throw new UserAlreadyPresentException("User already registered");
        }
        userRepository.save(new UserEntity(userDTO));
    }

    public Boolean areCredentialsValid(UserDTO userDTO) {

        Optional<UserEntity> check = userRepository.findByEmail(userDTO.getEmail());
        UserEntity userEntity;
        if (!check.isPresent())
            return false;
        else
        {
            userEntity = check.get();
        }
        //if (userEntity.getEmail().equals(userDTO.getEmail()) && )
        return false;
    }

    private Boolean isUserPresent(String email) {
        Optional<UserEntity> check = userRepository.findByEmail(email);
        return check.isPresent();
    }


}
