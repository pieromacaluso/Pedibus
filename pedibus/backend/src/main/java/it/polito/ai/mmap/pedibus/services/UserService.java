package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
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

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JwtTokenService jwtTokenService;
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
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private GMailService gMailService;
    @Autowired
    private FermataRepository fermataRepository;

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
        UserEntity userEntity = check.get();
        return userEntity;
    }

    public boolean checkMailIsPresent(String email) throws UsernameNotFoundException {
        Optional<UserEntity> check = userRepository.findByUsername(email);
        return check.isPresent();
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
                throw new UserAlreadyPresentException("Utente già registrato");

            }
        } else {
            RoleEntity userRole = roleRepository.findByRole("ROLE_USER");
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
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);
        userEntity.ifPresent(entity -> roles.addAll(entity.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList())));
        /*userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found")).getRoles());*/
        return jwtTokenService.createToken(username, roles);
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
        if (check.isPresent()) {
            userEntity = check.get();
            userRepository.save(userEntity);
            logger.info("SuperAdmin configurato ed abilitato.");
        }
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public List<ChildEntity> getAllChildren() {
        return childRepository.findAll();
    }

    public List<String> getAllChildrenId() {
        return childRepository.findAll().stream().map(ChildEntity::getCodiceFiscale).collect(Collectors.toList());
    }

    public List<ChildDTO> getAllChildrenById(List<String> childrenId) {
        List<ChildEntity> childrenEntities = new ArrayList<>();

        for (String cf : childrenId) {
            Optional<ChildEntity> c = childRepository.findByCodiceFiscale(cf);
            if (c.isPresent()) {
                ChildEntity childEntity = c.get();
                childrenEntities.add(childEntity);
            } else
                throw new ChildNotFoundException("Alunno non trovato");
        }

        return childrenEntities.stream().map(ChildDTO::new).collect(Collectors.toList());
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
//            RoleEntity[] roleArr = {roleRepository.findByRole("ROLE_USER"), roleRepository.findByRole("ROLE_ADMIN")};
            userEntity = new UserEntity(userID, new HashSet<>(Arrays.asList(roleRepository.findByRole("ROLE_ADMIN"))));
        } else {
            userEntity = check.get();
        }
        userEntity.getRoleList().add(roleRepository.findByRole("ROLE_ADMIN"));
        userRepository.save(userEntity);
    }

    public void delAdmin(String userID) {
        Optional<UserEntity> check = userRepository.findByUsername(userID);
        UserEntity userEntity;
        if (check.isPresent()) {
            userEntity = check.get();
            if (userEntity.getRoleList().contains(roleRepository.findByRole("ROLE_ADMIN")))
                userEntity.getRoleList().remove(roleRepository.findByRole("ROLE_ADMIN"));

            userRepository.save(userEntity);
        }
    }

    public void createRoles() {
        ArrayList<String> roles = new ArrayList<>();
        // Per aggiungere un ruolo di default basta aggiungere una add qui sotto.
        // roles.add("ROLE_XYZ");
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_SYSTEM-ADMIN");
        for (String role : roles) {
            RoleEntity roleEntity = roleRepository.findRoleEntityByRole(role);
            if (roleEntity == null)
                roleRepository.save(RoleEntity.builder().role(role).build());
        }
    }

    /**
     * Metodo che permette ad un genitore di registrare un suo figlio
     *
     * @param childDTO
     */
    public void registerChild(ChildDTO childDTO) {
        Optional<ChildEntity> c = childRepository.findById(childDTO.getCodiceFiscale());
        if (c.isPresent())
            throw new ChildAlreadyPresentException("Alunno con stesso codice fiscale trovato");
        Optional<FermataEntity> checkFerm = fermataRepository.findById(childDTO.getIdFermataDefault());
        if (!checkFerm.isPresent()) {
            throw new FermataNotFoundException();
        }
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ChildEntity childEntity = new ChildEntity(childDTO, principal.getId());
        principal.addChild(childDTO.getCodiceFiscale());
        childRepository.save(childEntity);
        userRepository.save(principal);
        logger.info("New Child " + childDTO.getCodiceFiscale() + " by user " + principal.getUsername());
    }

    /**
     * Metodo che permette di cambiare la fermata di default di un bambino o dal suo genitore o da un System-Admin
     *
     * @param idFermata
     * @param cf
     */
    public void updateChildStop(Integer idFermata, String cf) {
        Optional<ChildEntity> c = childRepository.findById(cf);
        if (c.isPresent()) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.getChildrenList().contains(cf) || principal.getRoleList().contains(roleRepository.findByRole("ROLE_SYSTEM-ADMIN"))) {
                if (fermataRepository.findById(idFermata).isPresent()) {
                    ChildEntity childEntity = c.get();
                    childEntity.setIdFermataDefault(idFermata);
                    childRepository.save(childEntity);
                } else
                    throw new FermataNotFoundException();

            } else
                throw new ChildNotFoundException("Bambino non trovato tra i tuoi figli");
        } else
            throw new ChildNotFoundException("Bambino non trovato");
    }

    /**
     * Metodo che permette di eliminare un bambino. Eseguibile o dal genitore o da un System-Admin
     *
     * @param idChild
     */
    public void delChild(String idChild) {
        Optional<ChildEntity> c = childRepository.findById(idChild);
        if (c.isPresent()) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ChildEntity childEntity = c.get();

            /*Rimozione dall'utente genitore*/
            if (principal.getChildrenList().contains(idChild)) {
                principal.getChildrenList().remove(childEntity);
                userRepository.save(principal);

            } else if (principal.getRoleList().contains(roleRepository.findByRole("ROLE_SYSTEM-ADMIN"))) {
                Optional<UserEntity> p = userRepository.findById(childEntity.getIdParent());
                if (p.isPresent()) {
                    UserEntity parent = p.get();
                    parent.getChildrenList().remove(childEntity);
                    userRepository.save(parent);
                }
            } else
                throw new ChildNotFoundException("Bambino non trovato tra i tuoi figli");
            /*Eliminazione entity*/
            childRepository.delete(childEntity);

        } else
            throw new ChildNotFoundException("Bambino non trovato");
    }
}
