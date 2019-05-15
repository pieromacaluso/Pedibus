package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.ActivationTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.exception.*;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.ActivationTokenRepository;
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
    private RecoverTokenRepository recoverTokenRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;
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
     * Metodo che gestisce la registrazione
     * salva su db e invia una mail di conferma
     * Gestisce 4 casistiche:
     * - Se l'utente arriva con una nuova mail
     * - Se la mail è già associata a un account
     * - Se la mail è già associata a un account, ma che non è stato attivato entro una deadline e quindi si permette il riutilizzo
     * - Se la mail è già associata a un account che ha privilegi di admin ed è inattivo (ad es record creato ad inzio anno dal preside)
     *
     * @param userDTO
     * @throws UserAlreadyPresentException
     */
    public void registerUser(UserDTO userDTO) throws UserAlreadyPresentException {
        UserEntity userEntity;
        Optional<UserEntity> check = userRepository.findByUsername(userDTO.getEmail());
        if (check.isPresent()) {
            userEntity = check.get();
            RoleEntity roleEntity = roleRepository.findByRole("ROLE_ADMIN");
            Optional<UserEntity> checkAdmin = userRepository.findByRoleListContainingAndUsernameAndIsEnabled(roleEntity, userDTO.getEmail(), false);
            Optional<ActivationTokenEntity> checkToken = activationTokenRepository.findByUserId(userEntity.getId());

            if (checkAdmin.isPresent()) {
                //Se la mail è già stata registrata come relativa a un account admin e l'account è inattivo
                userEntity = checkAdmin.get();
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userEntity.setCreationDate(MongoZonedDateTime.getNow());
            } else if (!userEntity.isEnabled() && !checkToken.isPresent()) {
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
        ActivationTokenEntity tokenEntity = new ActivationTokenEntity(userEntity.getId());
        logger.info(tokenEntity.getCreationDate().toString());
        activationTokenRepository.save(tokenEntity);
        String href = baseURL + "confirm/" + tokenEntity.getId();
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
        Optional<ActivationTokenEntity> checkToken = activationTokenRepository.findById(randomUUID);
        ActivationTokenEntity token;
        if (checkToken.isPresent()) {
            token = checkToken.get();
        } else {
            throw new TokenNotFoundException();
        }

        Optional<UserEntity> checkUser = userRepository.findById(token.getUserId());
        UserEntity userEntity;
        if (checkUser.isPresent()) {
            userEntity = checkUser.get();
        } else {
            throw new TokenNotFoundException();
        }
        if (!userEntity.isEnabled()) {
            userEntity.setEnabled(true);
            userEntity.setUserId(userEntity.getId().toString());
            userRepository.save(userEntity);
            activationTokenRepository.delete(token);
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
        ObjectId idToken = new ObjectId(randomUUID);
        Optional<RecoverTokenEntity> checkToken = recoverTokenRepository.findById(idToken);
        if (checkToken.isPresent()) {
            RecoverTokenEntity token = checkToken.get();
            Optional<UserEntity> checkUser = userRepository.findById(token.getUserId());
            if (checkUser.isPresent()) {
                UserEntity userEntity = checkUser.get();
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userRepository.save(userEntity);
                recoverTokenRepository.delete(token);
            } else {
                throw new RecoverProcessNotValidException();
            }

        } else {
            throw new RecoverProcessNotValidException();
        }
    }

    /**
     * Se la mail corrisponde a quella di un utente registrato invia una mail per iniziare il processo di recover
     *
     * @param email
     * @throws UsernameNotFoundException
     */
    public void recoverAccount(String email) throws RecoverProcessNotValidException {
        Optional<UserEntity> check = userRepository.findByUsernameAndIsEnabled(email, true);
        if (!check.isPresent())
            throw new UsernameNotFoundException("User not found");

        UserEntity userEntity = check.get();
        RecoverTokenEntity tokenEntity = new RecoverTokenEntity(userEntity.getId());
        logger.info(tokenEntity.getCreationDate().toString());
        recoverTokenRepository.save(tokenEntity);
        String href = baseURL + "recover/" + tokenEntity.getId();
        gMailService.sendMail(userEntity.getUsername(), "<p>Clicca per modificare la password</p><a href='" + href + "'>Reset your password</a>", RECOVER_ACCOUNT_SUBJECT);
        logger.info("Inviata recover email a: " + userEntity.getUsername());
    }

    public String getJwtToken(String username) {

        List<String> roles = new ArrayList<>();
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        userEntity.ifPresent(entity -> roles.addAll(entity.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList())));
        /*userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found")).getRoles());*/
        return jwtTokenService.createToken(username, roles);
    }


    /**
     * Permette di creare in automatico, se non presente, l'utente con privilegio SYSTEM-ADMIN. Tale utente è già abilitato senza l'invio dell'email.
     * TODO: da cancellare
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
        if(check.isPresent()) {
            userEntity = check.get();
            userEntity.setUserId(userEntity.getId().toString());
            userRepository.save(userEntity);
            logger.info("SuperAdmin configurato ed abilitato.");
        }
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
            RoleEntity[] roleArr = {roleRepository.findByRole("ROLE_USER"), roleRepository.findByRole("ROLE_ADMIN")};
            userEntity = new UserEntity(userID, new HashSet<>(Arrays.asList(roleArr)));
        } else {
            userEntity = check.get();
        }
        userEntity.getRoleList().add(roleRepository.findByRole("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }

    public void delAdmin(String userID) {
        Optional<UserEntity> check = userRepository.findByUsername(userID);
        UserEntity userEntity;
        if (!check.isPresent()) {
            RoleEntity[] roleArr = {roleRepository.findByRole("ROLE_USER")};
            userEntity = new UserEntity(userID, new HashSet<>(Arrays.asList(roleArr)));
        } else {
            userEntity = check.get();
        }
        if(userEntity.getRoleList().contains(roleRepository.findByRole("ROLE_ADMIN")))
            userEntity.getRoleList().remove(roleRepository.findByRole("ROLE_ADMIN"));

        userRepository.save(userEntity);
    }
}
