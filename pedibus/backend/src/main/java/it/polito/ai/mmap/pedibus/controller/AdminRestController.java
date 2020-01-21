package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.exception.SysAdminException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import it.polito.ai.mmap.pedibus.services.ChildService;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class AdminRestController {

    final
    Environment environment;
    final
    UserService userService;
    final
    ChildService childService;
    final
    LineeService lineeService;
    final
    RoleRepository roleRepository;
    // TODO: Logger in tutti gli endpoint
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public AdminRestController(Environment environment, UserService userService, ChildService childService, LineeService lineeService, RoleRepository roleRepository) {
        this.environment = environment;
        this.userService = userService;
        this.childService = childService;
        this.lineeService = lineeService;
        this.roleRepository = roleRepository;
    }

    /**
     * Endpoint per ottenere tutti i ruoli disponibili nel database
     *
     * @return Lista di Ruoli sottoforma di stringhe da assegnare agli utenti
     */
    @GetMapping("/sysadmin/roles")
    public List<String> getAvailableRole() {
        return userService.getAllRole();
    }


    /**
     * Enpoint per ottenere gli user paginati e filtrati attraverso le keywork specificate. Queste possono essere multiple
     * separate da una serie di spazi. Il servizio si occuperà di andare a splittare la stringa e ad effettuare la query
     * corretta al database.
     *
     * @param pageable Oggetto Pageable per catturare i parametri di paginazione espressi dall'utente
     * @param keyword  Elenco con parole separati da spazi da usare come filtro della ricerca. Ritorna tutti gli user se
     *                 vuota.
     * @return Pagina contenente una parte degli utenti presenti nel database.
     */
    @GetMapping("/sysadmin/users")
    public Page<UserInsertResource> getUsers(Pageable pageable, @RequestParam("keyword") String keyword) {
        return userService.getAllPagedUsers(pageable, keyword);
    }

    /**
     * Aggiunta di un nuovo utente al database da parte di un amministratore. Questo endpoint genererà un token
     * che verrà inviato all'utente, il quale potrà procedere con il cambio password e l'abilitazione dell'utente.
     *
     * @param userInsertResource DTO per inserimento Utente da parte dell'amministratore
     * @return UserEntity dell'utente inserito correttamente
     */
    @PostMapping("/sysadmin/users")
    public UserEntity insertUser(@Valid @ModelAttribute @RequestBody UserInsertResource userInsertResource, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_USER_ERROR);
        }
        return userService.insertUser(userInsertResource);
    }

    /**
     * Modifica dei dati di un utente già presente da parte dell'amministratore.
     *
     * @param userInsertResource l'utente i cui dettagli vanno aggiornati
     */
    @PutMapping("/sysadmin/users/{userId}")
    public void updateUser(@PathVariable("userId") String userId,
                           @Valid @ModelAttribute @RequestBody UserInsertResource userInsertResource,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.PUT_USER_ERROR);
        }
        userService.updateUser(userId, userInsertResource);
    }

    /**
     * Cancellazione di un utente da parte di un amministratore
     *
     * @param userId email dell'utente da eliminare
     */
    @DeleteMapping("/sysadmin/users/{userId}")
    public void deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUserByUsername(userId);
    }

    /**
     * Enpoint per ottenere i bambini paginati e filtrati attraverso le keywork specificate. Queste possono essere multiple
     * separate da una serie di spazi. Il servizio si occuperà di andare a splittare la stringa e ad effettuare la query
     * corretta al database.
     *
     * @param pageable Oggetto Pageable per catturare i parametri di paginazione espressi dall'utente
     * @param keyword  Elenco con parole separati da spazi da usare come filtro della ricerca. Ritorna tutti i bambini se
     *                 vuota.
     * @return Pagina contenente una parte dei bambini presenti nel database.
     */
    @GetMapping("/sysadmin/children")
    public Page<ChildDTO> getChildren(Pageable pageable, @RequestParam("keyword") String keyword) {
        return childService.getAllPagedChildren(pageable, keyword);
    }

    /**
     * Get di un singolo Bambino
     *
     * @param cfChild codice fiscale del bambino
     * @return ChildDTO
     */
    @GetMapping("/sysadmin/children/{idChild}")
    public ChildDTO getChild(@PathVariable("idChild") String cfChild) {
        return childService.getChildDTOById(cfChild);
    }

    /**
     * Aggiunta di un nuovo bambino al database da parte di un amministratore.
     *
     * @param childDTO      DTO per inserimento bambino da parte dell'amministratore
     * @param bindingResult validazione oggetto
     * @return ChildDTO dell'utente inserito correttamente
     */
    @PostMapping("/sysadmin/children")
    public ChildDTO createChild(@Valid @ModelAttribute @RequestBody ChildDTO childDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_CHILD_ERROR);
        }
        ChildEntity childEntity = childService.createChild(childDTO);
        return new ChildDTO(childEntity);
    }

    /**
     * Modifica di un bambino al database da parte di un amministratore.
     *
     * @param childId  id del bambino da modificare
     * @param childDTO Dati del bambino da modificare
     * @return ChildDTO del bambino modificato
     */
    @PutMapping("/sysadmin/children/{childId}")
    public ChildDTO updateChild(@PathVariable("childId") String childId,
                                @Valid @ModelAttribute @RequestBody ChildDTO childDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.PUT_CHILD_ERROR);
        }
        ChildEntity childEntity = childService.updateChild(childId, childDTO);
        return new ChildDTO(childEntity);
    }

    /**
     * Cancellazione bambino dal database da parte di un amministratore.
     *
     * @param childId codice fiscale del bambino da eliminare
     */
    @DeleteMapping("/sysadmin/children/{childId}")
    public void deleteChild(@PathVariable("childId") String childId) {
        childService.deleteChild(childId);
    }


    /**
     * TODO: ADMIN MASTER DI LINEA che non può essere declassato e che gestisce. Guarda ISSUE #59
     * Un admin di una linea o il system-admin inserisce un utente come admin per una linea, indicando
     * tramite PermissionResource.addOrDel se aggiungere(true) o eliminare(false) il permesso
     * Questo utente può essere già registrato o no e quando passerà attraverso il processo di registrazione
     * si troverà i privilegi di admin
     */
    @PutMapping("/admin/users/{userID}")
    public void setUserAdmin(@RequestBody PermissionResource permissionResource, @PathVariable("userID") String userID) {
        if (lineeService.isAdminLine(permissionResource.getIdLinea()) || userService.isSysAdmin()) {
            if (permissionResource.isAddOrDel()) {
                userService.addAdmin(userID);
                lineeService.addAdminLine(userID, permissionResource.getIdLinea());
            } else {
                userService.delAdmin(userID);
                lineeService.delAdminLine(userID, permissionResource.getIdLinea());
            }
        } else {
            throw new PermissionDeniedException();
        }
    }
}
