package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JwtTokenService jwtTokenService;
    @Autowired
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;
    @Autowired
    private GMailService gMailService;
    @Autowired
    LineeService lineeService;

    @Value("${mail.baseURL}")
    private String baseURL;
    @Value("${mail.registration_subject}")
    private String REGISTRATION_SUBJECT;
    @Value("${mail.recover_account_subject}")
    private String RECOVER_ACCOUNT_SUBJECT;


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
            throw new UsernameNotFoundException("Utente inesistente");
        }
        return check.get();
    }

    public void insertUser(UserInsertResource userInsertResource) {
        insertAdminLine(userInsertResource);
        userRepository.save(new UserEntity(userInsertResource.getUserId(), userInsertResource.getRoleIdList().stream().map(this::getRoleEntityById).collect(Collectors.toCollection(HashSet::new))));
    }

    public void updateUser(UserInsertResource userInsertResource) {
        UserEntity userEntity = ((UserEntity) loadUserByUsername(userInsertResource.getUserId()));
        lineeService.removeAdminFromAllLine(userInsertResource.getUserId());
        insertAdminLine(userInsertResource);
        userEntity.setRoleList(userInsertResource.getRoleIdList().stream().map(this::getRoleEntityById).collect(Collectors.toCollection(HashSet::new)));
        userRepository.save(userEntity);
    }

    private void insertAdminLine(UserInsertResource userInsertResource) {
        if (userInsertResource.getRoleIdList().contains("ROLE_ADMIN")) {
            userInsertResource.getLineaIdList().forEach(lineaId -> lineeService.addAdminLine(userInsertResource.getUserId(), lineaId));
        }
    }

    /**
     * @param idRole
     * @return una RoleEntity a partire dal suo identificativo
     */
    public RoleEntity getRoleEntityById(String idRole) {
        Optional<RoleEntity> checkRole = roleRepository.findById(idRole);
        if (checkRole.isPresent())
            return checkRole.get();
        else
            throw new IllegalStateException();
    }

    public List<String> getAllRole() {
        return roleRepository.findAll().stream().map(RoleEntity::getId).collect(Collectors.toList());
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
            RoleEntity roleEntity = getRoleEntityById("ROLE_ADMIN");
            Optional<UserEntity> checkAdmin = userRepository.findByRoleListContainingAndUsernameAndIsEnabled(roleEntity, userDTO.getEmail(), false);
            Optional<ActivationTokenEntity> checkToken = activationTokenRepository.findByUserId(userEntity.getId());

            if (checkAdmin.isPresent()) {
                //Se la mail è già stata registrata come relativa a un account admin e l'account è inattivo
                userEntity = checkAdmin.get();
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userEntity.setCreationDate(MongoTimeService.getNow());
            } else if (!userEntity.isEnabled() && !checkToken.isPresent()) {
                //Mail già associata a un account che non è stato abilitato in tempo
                userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                userEntity.setCreationDate(MongoTimeService.getNow());
            } else {
                throw new UserAlreadyPresentException("Utente già registrato");

            }
        } else {
            RoleEntity userRole = getRoleEntityById("ROLE_USER");
            userEntity = new UserEntity(userDTO, new HashSet<>(Arrays.asList(userRole)), passwordEncoder);

        }

        userRepository.save(userEntity);
        ActivationTokenEntity tokenEntity = new ActivationTokenEntity(userEntity.getId());
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
            throw new UsernameNotFoundException("Utente inesistente");

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
        UserEntity userEntity = (UserEntity) loadUserByUsername(username);
        roles.addAll(userEntity.getRoleList().stream().map(RoleEntity::getId).collect(Collectors.toList()));
        return jwtTokenService.createToken(username, roles);
    }


    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     * Se la mail dell'admin non corrisponde a nessun account ne creo uno vuoto con tali privilegi che poi l'utente quando si registra riempirà,
     *
     * @param userID
     */
    public void addAdmin(String userID) {
        Optional<UserEntity> check = userRepository.findByUsername(userID);
        UserEntity userEntity;
        if (!check.isPresent()) {
            userEntity = new UserEntity(userID, new HashSet<>(Collections.singletonList(getRoleEntityById("ROLE_ADMIN"))));
        } else {
            userEntity = check.get();
        }
        userEntity.getRoleList().add(getRoleEntityById("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }

    public void delAdmin(String userID) {
        UserEntity userEntity = (UserEntity) loadUserByUsername(userID);
        userEntity.getRoleList().remove(getRoleEntityById("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }

    public Boolean isSysAdmin() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getRoleList().contains(getRoleEntityById("ROLE_SYSTEM-ADMIN"));
    }

    public Boolean isAdmin() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getRoleList().contains(getRoleEntityById("ROLE_ADMIN"));
    }

    public Boolean isGuide() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getRoleList().contains(getRoleEntityById("ROLE_GUIDE"));
    }

    /**
     * Metodo da usare in altri service in modo da non dover fare sempre i controlli
     *
     * @param idParent
     * @return
     */
    public UserEntity getUserEntity(ObjectId idParent) {
        Optional<UserEntity> checkUser = userRepository.findById(idParent);
        if (checkUser.isPresent()) {
            return checkUser.get();
        } else
            throw new UserNotFoundException();
    }


}
