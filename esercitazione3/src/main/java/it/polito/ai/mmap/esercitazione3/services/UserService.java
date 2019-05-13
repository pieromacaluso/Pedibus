package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.*;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.RecoverTokenRepository;
import it.polito.ai.mmap.esercitazione3.repository.RoleRepository;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Value("${mail.baseURL}")
    private String baseURL;
    @Value("${mail.registration_subject}")
    private String REGISTRATION_SUBJECT;
    @Value("${mail.recover_account_subject}")
    private String RECOVER_ACCOUNT_SUBJECT;
    @Value("${mail.minutes_to_enable}")
    private int minuti;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private RecoverTokenRepository tokenRepository;

    @Autowired
    private GMailService gMailService;

    @Autowired
    JwtTokenService jwtTokenService;

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
        UserEntity userEntity;
        Optional<UserEntity> check = userRepository.findByUsername(userDTO.getEmail());
        if (check.isPresent()) {
            userEntity = check.get();
            RoleEntity roleEntity = roleRepository.findByRole("ROLE_ADMIN");
            Optional<UserEntity> checkAdmin = userRepository.findByRoleListContainingAndUsernameAndIsEnabled(roleEntity, userDTO.getEmail(), false);
            if (checkAdmin.isPresent()) {
                //Se la mail è già stata registrata come relativa a un account admin e l'account è inattivo
                userEntity = checkAdmin.get();
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userEntity.setCreationDate(MongoZonedDateTime.getNow());

            } else if (!userEntity.isEnabled() && (MongoZonedDateTime.getNow().getTime() - userEntity.getCreationDate().getTime()) > 1000 * 60 * minuti) {
                //Mail già associata a un account che non è stato abilitato in tempo
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userEntity.setCreationDate(MongoZonedDateTime.getNow());
            } else {
                throw new UserAlreadyPresentException("User already registered");

            }
        } else {
            RoleEntity userRole = roleRepository.findByRole("ROLE_USER");
            userEntity = new UserEntity(userDTO, new HashSet<>(Arrays.asList(userRole)), passwordEncoder);

        }


        userRepository.save(userEntity);
        String href = baseURL + "confirm/" + userEntity.getId();
        gMailService.sendMail(userEntity.getUsername(), "<p>Clicca per confermare account</p><a href='" + href + "'>Confirmation Link</a>", REGISTRATION_SUBJECT);
        logger.info("Inviata register email a: " + userEntity.getUsername());

    }

    /**
     * verifica che il codice random:
     * - corrisponda ad uno degli utenti in corso di verifica
     * - controlla che tale registrazione non sia scaduta
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
            throw new TokenNotFoundException();
        }

        if ((MongoZonedDateTime.getNow().getTime() - userEntity.getCreationDate().getTime()) < 1000 * 60 * minuti) {
            if (!userEntity.isEnabled()) {
                userEntity.setEnabled(true);
                userEntity.setUserId(userEntity.getId().toString());
                userRepository.save(userEntity);
            }
        } else {
            throw new RegistrationOOTException();
        }

    }

    /**
     * Metodo che ci permette di aggiornare la password di un utente
     * Verifica che il codice random:
     * - sia uno di quelli che abbiamo generato
     * - non sia scaduto.
     *
     * @param userDTO
     * @throws UsernameNotFoundException se non trova l'user
     */
    public void updateUserPassword(UserDTO userDTO, String randomUUID) throws UsernameNotFoundException {
        UserEntity userEntity = (UserEntity) loadUserByUsername(userDTO.getEmail());
        Optional<RecoverTokenEntity> checkToken = tokenRepository.findByUsername(userDTO.getEmail());
        if (checkToken.isPresent()) {
            if (checkToken.get().getTokenValue().equals(randomUUID)) {
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userRepository.save(userEntity);
            } else {
                throw new RecoverProcessNotValidException();
            }
        } else {
            throw new RecoverProcessNotValidException();
        }
    }


    public void recoverAccount(String email) throws UsernameNotFoundException {
        UserEntity userEntity = (UserEntity) loadUserByUsername(email); //se non esiste lancia un eccezione

        RecoverTokenEntity tokenEntity = new RecoverTokenEntity(userEntity.getUsername());
        logger.info(tokenEntity.getCreationDate().toString());
        tokenRepository.save(tokenEntity);
        String href = baseURL + "recover/" + tokenEntity.getTokenValue();
        gMailService.sendMail(userEntity.getUsername(), "<p>Clicca per modificare la password</p><a href='" + href + "'>Reset your password</a>", RECOVER_ACCOUNT_SUBJECT);
        logger.info("Inviata recover email a: " + userEntity.getUsername());
    }

    public String getJwtToken(String username) {

        List<String> roles = new ArrayList<>();
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        if (userEntity.isPresent()) {
            roles.addAll(userEntity.get().getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()));
        }

        String token = jwtTokenService.createToken(username, roles);/*userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found")).getRoles());*/
        return token;
    }


    /**
     * Permette di creare in automatico, se non presente, l'utente con privilegio SYSTEM-ADMIN. Tale utente è già abilitato senza l'invio dell'email.
     */
    public void registerSuperUser() {
        Optional<UserEntity> check = userRepository.findByUsername(superAdminMail);
        if (check.isPresent()) {
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(superAdminMail);
        userDTO.setPassword(superAdminPass);

        RoleEntity role = roleRepository.findByRole("ROLE_SYSTEM-ADMIN");

        UserEntity userEntity = new UserEntity(userDTO, new HashSet<>(Arrays.asList(role)), passwordEncoder);
        userEntity.setEnabled(true);
        userRepository.save(userEntity);

        check = userRepository.findByUsername(superAdminMail);      //rileggo per poter leggere l'objectId e salvarlo come string
        userEntity = check.get();
        userEntity.setUserId(userEntity.getId().toString());
        userRepository.save(userEntity);
        logger.info("SuperAdmin configurato ed abilitato.");
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Se la mail dell'admin non corrisponde a nessun account ne creo uno vuoto con tali privilegi che poi l'utente quando si registra riempirà,
     * se no lo creo da zero
     *
     * @param userID
     */
    public void addAdmin(String userID) {
        Optional<UserEntity> check = userRepository.findByUsername(userID);
        UserEntity userEntity;
        if (!check.isPresent()) {
            RoleEntity roleArr[] = {roleRepository.findByRole("ROLE_USER"), roleRepository.findByRole("ROLE_ADMIN")};
            userEntity = new UserEntity(userID, new HashSet<>(Arrays.asList(roleArr)));
        } else {
            userEntity = check.get();
        }
        userEntity.getRoleList().add(roleRepository.findByRole("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }


}
