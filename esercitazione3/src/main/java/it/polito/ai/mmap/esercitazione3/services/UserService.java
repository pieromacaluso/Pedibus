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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    private GMailSender gMailSender;

    /**
     * Metodo che ci restituisce un UserEntity a partire dall'email
     * Implementazione fissata dalla classe UserDetailsService
     *
     * @param email
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> check = userRepository.findByUsername(email);
        if (!check.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }
        UserEntity userEntity = check.get();
        return userEntity;
    }

    /**
     * Metodo che gestisce la registrazione
     * Lancia un eccezione nel caso ci sia già un utente con la mail indicata,
     * se no lo salva su db e invia una mail di conferma
     *
     * @param userDTO
     * @throws UserAlreadyPresentException
     */
    public void registerUser(UserDTO userDTO) throws UserAlreadyPresentException {
        Optional<UserEntity> check = userRepository.findByUsername(userDTO.getEmail());
        if (check.isPresent()) {
            throw new UserAlreadyPresentException("User already registered");
        }
        RoleEntity userRole = roleRepository.findByRole("user");
        UserEntity userEntity = new UserEntity(userDTO, new ArrayList<>(Arrays.asList(userRole)), passwordEncoder);
        userRepository.save(userEntity);
        new Thread(() -> gMailSender.sendEmail(userDTO)).start();
    }

    /**
     * Ci permette di abilitare l'account dopo che l'utente ha seguito l'url inviato per mail
     *
     * @param email
     */
    public void enableUser(String email) {
        UserEntity userEntity = (UserEntity) loadUserByUsername(email);
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
    }

    /**
     * Metodo che controlla la validità delle credenziali per un utente
     *
     * @param userDTO
     * @return
     */
    public Boolean areCredentialsValid(UserDTO userDTO) {

        Optional<UserEntity> check = userRepository.findByUsername(userDTO.getEmail());
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
