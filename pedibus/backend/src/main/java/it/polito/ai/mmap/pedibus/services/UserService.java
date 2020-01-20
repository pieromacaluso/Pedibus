package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.RecoverProcessNotValidException;
import it.polito.ai.mmap.pedibus.exception.TokenNotFoundException;
import it.polito.ai.mmap.pedibus.exception.UserAlreadyPresentException;
import it.polito.ai.mmap.pedibus.exception.UserNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.NewUserPassDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JwtTokenService jwtTokenService;
    @Autowired
    LineeService lineeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;
    @Autowired
    private NewUserTokenRepository newUserTokenRepository;
    @Autowired
    private GMailService gMailService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @Value("${mail.baseURL}")
    private String baseURL;
    @Value("${mail.registration_subject}")
    private String REGISTRATION_SUBJECT;
    @Value("${mail.recover_account_subject}")
    private String RECOVER_ACCOUNT_SUBJECT;
    @Value("${mail.new_user_account_subject}")
    private String NEW_USER_ACCOUNT_SUBJECT;

    public static String fromKeywordToRegex(String keyword) {
        List<String> keywords = Arrays.asList(keyword.split("\\s+"));
        StringBuilder regex = new StringBuilder("");
        for (int i = 0; i < keywords.size(); i++) {
            regex.append(".*").append(keywords.get(i)).append(".*");
            if (i != keywords.size() - 1) regex.append("|");
        }
        return regex.toString();
    }

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

    public UserEntity insertUser(UserInsertResource userInsertResource) {
        insertAdminLine(userInsertResource);
        UserEntity userEntity = new UserEntity(userInsertResource, userInsertResource.getRoleIdList().stream().map(this::getRoleEntityById).collect(Collectors.toCollection(HashSet::new)), passwordEncoder);
        lineeService.removeAdminFromAllLine(userInsertResource.getUserId());
        insertAdminLine(userInsertResource);
        userEntity = userRepository.save(userEntity);
        this.sendUpdateNotification();
        this.firstAccount(userEntity);
        return userEntity;
    }

    public void updateUser(String userId, UserInsertResource userInsertResource) {
        UserEntity userEntity = ((UserEntity) loadUserByUsername(userId));
        userEntity.setName(userInsertResource.getName());
        userEntity.setSurname(userInsertResource.getSurname());
        userEntity.setUsername(userInsertResource.getUserId());
        lineeService.removeAdminFromAllLine(userInsertResource.getUserId());
        insertAdminLine(userInsertResource);
        userEntity.setRoleList(userInsertResource.getRoleIdList().stream().map(this::getRoleEntityById).collect(Collectors.toCollection(HashSet::new)));
        userEntity.setChildrenList(userInsertResource.getChildIdList());
        userRepository.save(userEntity);
        this.sendUpdateNotification();
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

    /**
     * Se la mail corrisponde a quella di un utente registrato invia una mail per iniziare il processo di recover
     *
     * @param userEntity
     * @throws UsernameNotFoundException
     */
    public void firstAccount(UserEntity userEntity) throws RecoverProcessNotValidException {
        Optional<UserEntity> check = userRepository.findByUsername(userEntity.getUsername());
        if (!check.isPresent())
            throw new UsernameNotFoundException("Utente inesistente");

        NewUserTokenEntity tokenEntity = new NewUserTokenEntity(userEntity.getId());
        logger.info(tokenEntity.getCreationDate().toString());
        newUserTokenRepository.save(tokenEntity);
        String href = baseURL + "new-user/" + tokenEntity.getId();
        gMailService.sendMail(userEntity.getUsername(),
                "<p>Il tuo account è stato appena creato con le seguenti credenziali:</p>" +
                        "<p><ul>" +
                        "<li><b>Username</b>: " + userEntity.getUsername() + "</li>" +
                        "<li><b>Password</b>: " + userEntity.getSurname() + userEntity.getName() + userEntity.getUsername().length() + "</li>" +
                        "</ul></p>" +
                        "<p>Clicca per modificare la password e attivare il tuo account <a href='" + href + "'>Reset your password</a></p>", NEW_USER_ACCOUNT_SUBJECT);
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
//    public void addAdmin(String userID) {
//        Optional<UserEntity> check = userRepository.findByUsername(userID);
//        UserEntity userEntity;
//        if (!check.isPresent()) {
//            userEntity = new UserEntity(userID, new HashSet<>(Collections.singletonList(getRoleEntityById("ROLE_ADMIN"))), passwordEncoder);
//        } else {
//            userEntity = check.get();
//        }
//        userEntity.getRoleList().add(getRoleEntityById("ROLE_ADMIN"));
//        userRepository.save(userEntity);
//    }

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

    public Page<UserInsertResource> getAllPagedUsers(Pageable pageable, String keyword) {
        Page<UserEntity> pagedUsersEntity = userRepository.searchByNameSurnameCF(UserService.fromKeywordToRegex(keyword), pageable);
        return PageableExecutionUtils.getPage(pagedUsersEntity.stream().map((e) -> new UserInsertResource(e, this.lineeService.getAdminLineForUser(e.getUsername()))).collect(Collectors.toList()), pageable, pagedUsersEntity::getTotalElements);
    }

    public void deleteUserByUsername(String userId) {
        Optional<UserEntity> u = this.userRepository.findByUsername(userId);
        if (!u.isPresent()) {
            throw new UserNotFoundException();
        }
        this.userRepository.deleteById(u.get().getId());
        this.sendUpdateNotification();
    }

    private void sendUpdateNotification() {
        Optional<RoleEntity> roleEntity = this.roleRepository.findById("ROLE_SYSTEM-ADMIN");
        if (roleEntity.isPresent()) {
            Optional<List<UserEntity>> userEntities = this.userRepository.findAllByRoleListContaining(roleEntity.get());
            if (userEntities.isPresent()) {
                for (UserEntity admin : userEntities.get())
                    this.simpMessagingTemplate.convertAndSendToUser(admin.getUsername(), "/anagrafica", "updates");
            }
        }
    }

    public void updateNewUserPasswordAndEnable(NewUserPassDTO newUserPassDTO, String randomUUID) {
        Optional<NewUserTokenEntity> checkToken = newUserTokenRepository.findById(new ObjectId(randomUUID));
        NewUserTokenEntity token;
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
        if (!passwordEncoder.matches(newUserPassDTO.getOldPassword(), userEntity.getPassword())) {
            throw new TokenNotFoundException();
        }
        userEntity.setPassword(passwordEncoder.encode(newUserPassDTO.getPassword()));

        if (!userEntity.isEnabled()) {
            userEntity.setEnabled(true);
            userRepository.save(userEntity);
            newUserTokenRepository.delete(token);
        }

    }
}
