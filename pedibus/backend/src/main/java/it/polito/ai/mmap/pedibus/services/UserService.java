package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.NewUserPassDTO;
import it.polito.ai.mmap.pedibus.objectDTO.RecoverUserDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
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
    private NotificheService notificheService;


    @Value("${mail.baseURL}")
    private String baseURL;
    @Value("${mail.registration_subject}")
    private String REGISTRATION_SUBJECT;
    @Value("${mail.recover_account_subject}")
    private String RECOVER_ACCOUNT_SUBJECT;
    @Value("${mail.new_user_account_subject}")
    private String NEW_USER_ACCOUNT_SUBJECT;
    @Value("${superadmin.email}")
    private String superAdminMail;

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
     * @param email email
     * @return UserDetails dettagli dell'utente
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByUsername(email).orElseThrow(UserNotFoundException::new);
    }

    /**
     * Inserimento di un utente da parte di un Amministratore. Questo processo va ad inizializzare completamente l'utente
     * lasciando solo il boolean di abilitazione su false. L'account infatti non sarà attivo fino a quando l'utente non
     * avrà completato la procedura di cambio password iniziale.
     *
     * @param userInsertResource DTO per inserimento Utente da parte dell'amministratore
     * @return UserEntity dell'utente inserito correttamente
     */
    public UserEntity insertUser(UserInsertResource userInsertResource) {
        if (userInsertResource.getRoleIdList().contains("ROLE_SYSTEM-ADMIN"))
            throw new PermissionDeniedException();
        Optional<UserEntity> u = this.userRepository.findByUsername(userInsertResource.getUserId());
        if (u.isPresent())
            throw new UserAlreadyPresentException(userInsertResource.getUserId());
        lineeService.removeAdminFromAllLine(userInsertResource.getUserId());
        insertAdminLine(userInsertResource);
        UserEntity userEntity = new UserEntity(userInsertResource,
                userInsertResource.getRoleIdList().stream()
                        .map(this::getRoleEntityById)
                        .collect(Collectors.toCollection(HashSet::new)), passwordEncoder);
        userEntity = userRepository.save(userEntity);
        this.notificheService.sendUpdateNotification();
        this.firstAccount(userEntity);
        return userEntity;
    }

    /**
     * Funzione che permette all'amministratore di modificare l'utente.
     *
     * @param userId             indirizzo email che dovrebbe essere presente nel database, utilizzato per ottenere l'entity da
     *                           modificare
     * @param userInsertResource DTO utente con i nuovi dati.
     */
    public void updateUser(String userId, UserInsertResource userInsertResource) {
        if (userId.equals(superAdminMail) || userInsertResource.getRoleIdList().contains("ROLE_SYSTEM-ADMIN"))
            throw new PermissionDeniedException();
        UserEntity userEntity = ((UserEntity) loadUserByUsername(userId));
        userEntity.setName(userInsertResource.getName());
        userEntity.setSurname(userInsertResource.getSurname());
        userEntity.setUsername(userInsertResource.getUserId());
        lineeService.removeAdminFromAllLine(userInsertResource.getUserId());
        insertAdminLine(userInsertResource);
        userEntity.setRoleList(userInsertResource.getRoleIdList().stream().map(this::getRoleEntityById).collect(Collectors.toCollection(HashSet::new)));

        if (userInsertResource.getRoleIdList().contains("ROLE_USER"))
            userEntity.setChildrenList(userInsertResource.getChildIdList());
        else
            userEntity.setChildrenList(Collections.emptySet());

        userRepository.save(userEntity);
        this.notificheService.sendUpdateNotification();
    }

    /**
     * Se l'utente è un amministratore di linea, questa funzione va ad aggiungerlo come amministratore di linea nelle
     * linee indicate dal DTO User di amministrazione comunicato.
     *
     * @param userInsertResource DTO per inserimento Utente da parte dell'amministratore
     */
    private void insertAdminLine(UserInsertResource userInsertResource) {
        if (userInsertResource.getRoleIdList().contains("ROLE_ADMIN")) {
            userInsertResource.getLineaIdList().forEach(lineaId -> lineeService.addAdminLine(userInsertResource.getUserId(), lineaId));
        }
        List<LineaEntity> linesMaster = this.lineeService.getAllLinesMasterMail(userInsertResource.getUserId());
        if (!linesMaster.isEmpty()) {
            linesMaster.forEach(lineaEntity -> lineeService.addAdminLine(userInsertResource.getUserId(), lineaEntity.getId()));
            userInsertResource.getRoleIdList().add("ROLE_ADMIN");
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

    /**
     * Funzione che restituisce tutti i ruoli disponibili nel Database
     *
     * @return Lista di Stringhe, ovvero ID ruoli
     */
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
     * @param userDTO userDTO
     * @throws UserAlreadyPresentException Eccezione utente già presente
     * @deprecated Flusso di registrazione usato nelle registrazioni
     */
    @Deprecated
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
                throw new UserAlreadyPresentException(userDTO.getEmail());
            }
        } else {
            RoleEntity userRole = getRoleEntityById("ROLE_USER");
            userEntity = new UserEntity(userDTO, new HashSet<>(Arrays.asList(userRole)), passwordEncoder);
        }
        userRepository.save(userEntity);
        ActivationTokenEntity tokenEntity = new ActivationTokenEntity(userEntity.getId());
        activationTokenRepository.save(tokenEntity);
        String href = baseURL + "confirm/" + tokenEntity.getId();
        gMailService.sendMail(userEntity.getUsername(), PedibusString.CONFIRMATION_MAIL(href), REGISTRATION_SUBJECT);
        logger.info(PedibusString.MAIL_SENT("conferma", userEntity.getUsername()));
    }

    /**
     * verifica che il codice random:
     * - corrisponda ad uno degli utenti in corso di verifica
     * - controlla che tale registrazione non sia scaduta
     * <p>
     * tutto ok = porta utente allo stato attivo e restituisce 200 – Ok
     * altrimenti = restituisce 404 – Not found
     *
     * @param randomUUID token di conferma
     * @deprecated Flusso di conferma non più utilizzato
     */
    @Deprecated
    public void enableUser(ObjectId randomUUID) {
        Optional<ActivationTokenEntity> checkToken = activationTokenRepository.findById(randomUUID);
        ActivationTokenEntity token;
        token = checkToken.orElseThrow(TokenNotFoundException::new);

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
     * @param userDTO    dati dell'utente
     * @param randomUUID Token
     */
    public void updateUserPassword(RecoverUserDTO userDTO, String randomUUID) {
        ObjectId idToken = new ObjectId(randomUUID);
        Optional<RecoverTokenEntity> checkToken = recoverTokenRepository.findById(idToken);
        RecoverTokenEntity token = checkToken.orElseThrow(TokenNotFoundException::new);
        Optional<UserEntity> checkUser = userRepository.findById(token.getUserId());
        UserEntity userEntity = checkUser.orElseThrow(UserNotFoundException::new);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(userEntity);
        recoverTokenRepository.delete(token);
    }

    /**
     * Funzione che elimina un utente dal database e manda una notifica per aggiornare il cambiamento per tutti gli admin
     *
     * @param userId email dell'utente da eliminare
     */
    public void deleteUserByUsername(String userId) {
        UserEntity u = this.userRepository.findByUsername(userId).orElseThrow(UserNotFoundException::new);
        if (u.getRoleList().contains(getRoleEntityById("ROLE_SYSTEM-ADMIN")))
            throw new PermissionDeniedException();
        this.userRepository.deleteById(u.getId());
        this.notificheService.sendUpdateNotification();
    }

    /**
     * Se la mail corrisponde a quella di un utente registrato invia una mail per iniziare il processo di recover
     *
     * @param email email dell'utente da recuperare
     */
    public void recoverAccount(String email) {
        Optional<UserEntity> check = userRepository.findByUsernameAndIsEnabled(email, true);
        if (!check.isPresent())
            throw new UserNotFoundException();

        UserEntity userEntity = check.get();
        RecoverTokenEntity tokenEntity = new RecoverTokenEntity(userEntity.getId());
        logger.info(tokenEntity.getCreationDate().toString());
        recoverTokenRepository.save(tokenEntity);
        String href = baseURL + "recover/" + tokenEntity.getId();
        gMailService.sendMail(userEntity.getUsername(), PedibusString.RECOVER_MAIL(href), RECOVER_ACCOUNT_SUBJECT);
        logger.info("Inviata recover email a: " + userEntity.getUsername());
    }

    /**
     * Processo di inizializzazione dell'utente. Viene inviata una mail all'utente appena registrato dall'admin per
     * permettegli di cambiare password e poter abilitare le funzionalità dell'applicazione.
     *
     * @param userEntity Oggetto UserEntity
     */
    public void firstAccount(UserEntity userEntity) {
        Optional<UserEntity> check = userRepository.findByUsername(userEntity.getUsername());
        if (!check.isPresent())
            throw new UserNotFoundException();

        NewUserTokenEntity tokenEntity = new NewUserTokenEntity(userEntity.getId());
        logger.info(tokenEntity.getCreationDate().toString());
        newUserTokenRepository.save(tokenEntity);
        String href = baseURL + "new-user/" + tokenEntity.getId();
        gMailService.sendMail(userEntity.getUsername(), PedibusString.NEW_USER_MAIL(href, userEntity),
                NEW_USER_ACCOUNT_SUBJECT);
        logger.info(PedibusString.MAIL_SENT("registrazione", userEntity.getUsername()));
    }

    /**
     * Da username a JWT token
     *
     * @param username email
     * @return string con JWT Token
     */
    public String getJwtToken(String username) {
        List<String> roles = new ArrayList<>();
        UserEntity userEntity = (UserEntity) loadUserByUsername(username);
        roles.addAll(userEntity.getRoleList().stream().map(RoleEntity::getId).collect(Collectors.toList()));
        return jwtTokenService.createToken(username, roles);
    }

    /**
     * Ottieni tutti gli utenti nel database
     *
     * @return Lista di tutti gli utenti
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Aggiunta privilegio di amministratore di linea
     *
     * @param email email
     */
    public void addAdmin(String email) {
        UserEntity userEntity = (UserEntity) loadUserByUsername(email);
        userEntity.getRoleList().add(getRoleEntityById("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }

    /**
     * Aggiunta privilegio di amministratore di linea
     *
     * @param email email dell'utente
     */
    public void delAdmin(String email) {
        UserEntity userEntity = (UserEntity) loadUserByUsername(email);
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

    public boolean isUser() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getRoleList().contains(getRoleEntityById("ROLE_USER"));
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

    /**
     * Restituisce tutti gli utenti paginati
     *
     * @param pageable oggetto Pageable che contiene query per paginazione
     * @param keyword  Elenco di keyword di ricerca separate da spazi
     * @return Pagina utente richiesta
     */
    public Page<UserInsertResource> getAllPagedUsers(Pageable pageable, String keyword) {
        Page<UserEntity> pagedUsersEntity = userRepository.searchByNameSurnameCfNoSysAdmin(UserService.fromKeywordToRegex(keyword), this.superAdminMail, pageable);
        return PageableExecutionUtils.getPage(pagedUsersEntity.stream()
                .map((e) -> new UserInsertResource(e, this.lineeService.getAdminLineForUser(e.getUsername())))
                .collect(Collectors.toList()), pageable, pagedUsersEntity::getTotalElements);
    }


    /**
     * Aggiorna l'utente creato dall'amministratore con le nuove credenziali e il token fornito. Abilita l'utente.
     *
     * @param newUserPassDTO NewUserPassDTO con i dati
     * @param randomUUID     newUser token
     */
    public void updateNewUserPasswordAndEnable(NewUserPassDTO newUserPassDTO, String randomUUID) {
        Optional<NewUserTokenEntity> checkToken = newUserTokenRepository.findById(new ObjectId(randomUUID));
        NewUserTokenEntity token;
        token = checkToken.orElseThrow(TokenNotFoundException::new);
        Optional<UserEntity> checkUser = userRepository.findById(token.getUserId());
        UserEntity userEntity;
        userEntity = checkUser.orElseThrow(TokenNotFoundException::new);
        userEntity.setPassword(passwordEncoder.encode(newUserPassDTO.getPassword()));

        if (!userEntity.isEnabled()) {
            userEntity.setEnabled(true);
            userRepository.save(userEntity);
            newUserTokenRepository.delete(token);
        }

    }


    public List<UserInsertResource> getAllGuidesAdmin() throws RoleNotFoundException {
        List<UserEntity> userEntities =
                this.userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(
                        this.roleRepository.findById("ROLE_GUIDE").orElseThrow(RoleNotFoundException::new));
        userEntities.addAll(this.userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(
                this.roleRepository.findById("ROLE_ADMIN").orElseThrow(RoleNotFoundException::new)));
        return userEntities.stream().distinct().map(e -> new UserInsertResource(e, this.lineeService.getAdminLineForUser(e.getUsername()))).collect(Collectors.toList());
    }

    public UserInsertResource getUserByEmail(String email) {
        UserEntity user = this.userRepository.findByUsername(email).orElseThrow(UserNotFoundException::new);
        return new UserInsertResource(user, this.lineeService.getAdminLineForUser(user.getUsername()));
    }

    /**
     * Un admin di una linea o il system-admin inserisce un utente come admin per una linea, indicando
     * tramite PermissionResource.addOrDel se aggiungere(true) o eliminare(false) il permesso
     * Un admin non può togliersi da solo i privilegi
     *
     * @param permissionResource Linea e booleano
     * @param mail               email
     */
    public void setUserAdmin(PermissionResource permissionResource, String mail) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((lineeService.isAdminLine(permissionResource.getIdLinea()) && !lineeService.isMasterLine(mail, permissionResource.getIdLinea()) && !principal.getUsername().equals(mail))
                || this.isSysAdmin()) {
            UserInsertResource userEntity = this.getUserByEmail(mail);
            if (permissionResource.isAddOrDel()) {
                this.addAdmin(userEntity.getUserId());
                lineeService.addAdminLine(userEntity.getUserId(), permissionResource.getIdLinea());
                this.notificheService.generatePromotionNotification(userEntity.getUserId(), permissionResource);
                this.notificheService.sendUpdateNotification();
            } else {
                lineeService.delAdminLine(userEntity.getUserId(), permissionResource.getIdLinea());
                userEntity.getLineaIdList().remove(permissionResource.getIdLinea());
                if (userEntity.getLineaIdList().isEmpty())
                    this.delAdmin(userEntity.getUserId());
                this.notificheService.generatePromotionNotification(userEntity.getUserId(), permissionResource);
                this.notificheService.sendUpdateNotification();
            }
        } else {
            throw new PermissionDeniedException();
        }
    }

}
