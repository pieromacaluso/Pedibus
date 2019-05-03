package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.UserAlreadyPresentException;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.RoleRepository;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import org.bson.types.ObjectId;
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
    private GMailService gMailService;

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
     * Metodo che controlla la validità delle credenziali per un utente
     *
     * @param userDTO
     * @return
     */
    public Boolean isLoginValid(UserDTO userDTO) {
        UserEntity userEntity;
        try {
            userEntity = (UserEntity) loadUserByUsername(userDTO.getEmail());
        } catch (UsernameNotFoundException e) {
            logger.info("Login fail - Utente non trovato");
            return false;
        }

        if (userEntity.isEnabled() && passwordEncoder.matches(userDTO.getPassword(), userEntity.getPassword())) {
            logger.info("Utente loggato correttamente");
            return true;
        } else {
            logger.info("Login fail - password non matchano o utente non abilitato");
            return false;
        }
    }

    /**
     * Metodo che gestisce la registrazione
     * salva su db e invia una mail di conferma
     *
     * @param userDTO
     * @throws UserAlreadyPresentException nel caso ci sia già un utente con la mail indicata
     */
    public void registerUser(UserDTO userDTO) throws UserAlreadyPresentException {
        Optional<UserEntity> check = userRepository.findByUsername(userDTO.getEmail());
        if (check.isPresent()) {
            throw new UserAlreadyPresentException("User already registered");
        }
        RoleEntity userRole = roleRepository.findByRole("user");
        UserEntity userEntity = new UserEntity(userDTO, new ArrayList<>(Arrays.asList(userRole)), passwordEncoder);
        userEntity = userRepository.save(userEntity);
        gMailService.sendRegisterEmail(userEntity);
    }

    /**
     * verifica che il codice random:
     * - corrisponda ad uno degli utenti in corso di verifica -DONE
     * - controlla che tale registrazione non sia scaduta -TODO
     * <p>
     * tutto ok -> porta utente allo stato attivo e restituisce 200 – Ok
     * altrimenti -> restituisce 404 – Not found
     *
     * @param randomUUID
     */
    public void enableUser(ObjectId randomUUID) {
        Optional<UserEntity> check = userRepository.findById(randomUUID);
        UserEntity userEntity;
        if (check.isPresent()) {
            userEntity = check.get();
        } else {
            return; //TODO uuid non riconosciuto
        }
        if (!userEntity.isEnabled()) {
            userEntity.setEnabled(true);
            userRepository.save(userEntity);
        } else
            return; //TODO già confermato
    }


    /**
     * Metodo che ci permette di aggiornare la password di un utente
     * Verifica che il codice random:
     * - sia uno di quelli che abbiamo generato
     * - non sia scaduto. //TODO
     *
     * @param userDTO
     * @throws UsernameNotFoundException se non trova l'user
     */
    public void updateUserPassword(UserDTO userDTO, ObjectId randomUUID) throws UsernameNotFoundException {
        UserEntity userEntity = (UserEntity) loadUserByUsername(userDTO.getEmail());

        if (userEntity.getId().equals(randomUUID)) {
            userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userEntity = userRepository.save(userEntity);
        } else {
            return; //TODO 404
        }
    }





    public void recoverAccount(String email) throws UsernameNotFoundException {
        UserEntity userEntity = (UserEntity) loadUserByUsername(email); //se non esiste lancia un eccezione
        gMailService.sendRecoverEmail(userEntity);
    }
}
