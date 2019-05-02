package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.UserAlreadyPresentException;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserRepository userRepository;

    public void registerUser(UserDTO userDTO) {
        if (isUserPresent(userDTO.getEmail())) {
            throw new UserAlreadyPresentException("User already registered");
        }
        userRepository.save(new UserEntity(userDTO));
    }

    private Boolean isUserPresent(String email) {
        Optional<UserEntity> check = userRepository.findByEmail(email);
        return check.isPresent();
    }


}
