package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.SysAdminException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import it.polito.ai.mmap.pedibus.services.ChildService;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import javax.validation.Valid;
import java.util.List;

@RestController
@ApiOperation("Endpoint dedicati al sysadmin")
public class AdminRestController {
    @Autowired
    Environment environment;
    @Autowired
    UserService userService;
    @Autowired
    ChildService childService;
    @Autowired
    LineeService lineeService;
    @Autowired
    RoleRepository roleRepository;
    // TODO: Logger in tutti gli endpoint
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Endpoint per ottenere tutti i ruoli disponibili nel database
     *
     * @return Lista di Ruoli sottoforma di stringhe da assegnare agli utenti
     */
    @GetMapping("/sysadmin/roles")
    @ApiOperation("Restituisce tutti i ruoli disponibili sul database")
    public List<String> getAvailableRole() {
        return userService.getAllRole();
    }


    /**
     * Enpoint per ottenere gli utenti paginati e filtrati attraverso le keyword specificate. Queste possono essere multiple
     * separate da una serie di spazi. Il servizio si occuperà di andare a splittare la stringa e ad effettuare la query
     * corretta al database.
     *
     * @param pageable Oggetto Pageable per catturare i parametri di paginazione espressi dall'utente
     * @param keyword  Elenco con parole separati da spazi da usare come filtro della ricerca. Ritorna tutti gli user se
     *                 vuota.
     * @return Pagina contenente una parte degli utenti presenti nel database.
     */
    @GetMapping("/sysadmin/users")
    @ApiOperation("Restituisce gli utenti paginati e filtrati attraverso le keyword specificate")
    public Page<UserInsertResource> getUsers(Pageable pageable, @ApiParam(name = "keyword", value = "Filtra gli user restituiti. Possono essere più di una separate da spazi") @RequestParam(name = "keyword") String keyword) {
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
    @ApiOperation("Aggiunge un nuovo utente al database, inviando una mail per attivare l'account")
    public UserEntity insertUser(@ApiParam(name = "userInsertResource", value = "Contiene i dettagli dell'utente che verrà inserito") @Valid @RequestBody UserInsertResource userInsertResource,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_USER_ERROR);
        }
        return userService.insertUser(userInsertResource);
    }

    /**
     * Modifica i dati di un utente già creato, da parte dell'amministratore.
     *
     * @param userInsertResource l'utente i cui dettagli vanno aggiornati
     */
    @PutMapping("/sysadmin/users/{userId}")
    @ApiOperation("Modifica i dati di un utente già creato")
    public void updateUser(@ApiParam(name = "userId", value = "L'ID dell'utente da modificare") @PathVariable("userId") String userId,
                           @ApiParam(name = "userInsertResource", value = "Contiene i dati aggiornati dell'utente") @Valid @RequestBody UserInsertResource userInsertResource,
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
    @ApiOperation("Cancella l'utente specificato")
    public void deleteUser(@ApiParam(name = "userId", value = "L'ID dell'utente da cancellare") @PathVariable("userId") String userId) {
        userService.deleteUserByUsername(userId);
    }

    /**
     * Enpoint per ottenere i bambini paginati e filtrati attraverso le keyword specificate. Queste possono essere multiple
     * separate da una serie di spazi. Il servizio si occuperà di andare a splittare la stringa e ad effettuare la query
     * corretta al database.
     *
     * @param pageable Oggetto Pageable per catturare i parametri di paginazione espressi dall'utente
     * @param keyword  Elenco con parole separati da spazi da usare come filtro della ricerca. Ritorna tutti i bambini se
     *                 vuota.
     * @return Pagina contenente una parte dei bambini presenti nel database.
     */
    @GetMapping("/sysadmin/children")
    @ApiOperation("Restituisce i bambini paginati e filtrati attraverso le keyword specificate")
    public Page<ChildDTO> getChildren(Pageable pageable, @ApiParam(name = "keyword", value = "Filtra gli user restituiti. Possono essere più di una separate da spazi") @RequestParam("keyword") String keyword) {
        return childService.getAllPagedChildren(pageable, keyword);
    }

    /**
     * Get di un singolo Bambino
     *
     * @param cfChild codice fiscale del bambino
     * @return ChildDTO
     */
    @GetMapping("/sysadmin/children/{idChild}")
    @ApiOperation("Restituisce il bambino specificato")
    public ChildDTO getChild(@ApiParam(name = "idChild", value = "Il codice fiscale del bambino") @PathVariable("idChild") String cfChild) {
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
    @ApiOperation("Aggiunge un nuovo bambino al database")
    public ChildDTO createChild(@ApiParam(name = "childDTO", value = "Contiene i dettagli del bambino che verrà inserito") @Valid @RequestBody ChildDTO childDTO,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_CHILD_ERROR);
        }
        ChildEntity childEntity = childService.createChild(childDTO);
        return new ChildDTO(childEntity);
    }

    /**
     * Modifica di un bambino sul database da parte di un amministratore.
     *
     * @param childId  id del bambino da modificare
     * @param childDTO Dati del bambino da modificare
     * @return ChildDTO del bambino modificato
     */
    @PutMapping("/sysadmin/children/{childId}")
    @ApiOperation("Modifica i dati di un bambino già creato")
    public ChildDTO updateChild(@ApiParam(name = "userId", value = "Il codice fiscale del bambino da modificare") @PathVariable("childId") String childId,
                                @ApiParam(name = "childDTO", value = "Contiene i dati aggiornati del bambino") @Valid @RequestBody ChildDTO childDTO,
                                BindingResult bindingResult) {
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
    @ApiOperation("Cancella il bambino specificato")
    public void deleteChild(@ApiParam(name = "childId", value = "Il codice fiscale del bambino da cancellare") @PathVariable("childId") String childId) {
        childService.deleteChild(childId);
    }

    /**
     * Un admin di una linea o il system-admin inserisce un utente come admin per una linea, indicando
     * tramite PermissionResource.addOrDel se aggiungere(true) o eliminare(false) il permesso
     * Questo utente può essere già registrato o no e quando passerà attraverso il processo di registrazione
     * si troverà i privilegi di admin
     * todo mi sembra di capire che non c'è più questa cosa e questa operazione su un utente non salvato su db causa una UserNotFoundException (marcof)
     *
     * @param permissionResource Linea e booleano
     * @param userId             L'id dell'utente
     */
    @PutMapping("/admin/users/{userID}")
    @ApiOperation("Aggiunge o rimuove il ruolo di admin per un utente")
    public void setUserAdmin(@ApiParam(name = "permissionResource", value = "Contiene i dettagli dei permessi da assegnare al dato utente") @RequestBody PermissionResource permissionResource,
                             @ApiParam(name = "userId", value = "L'ID dell'utente") @PathVariable("userID") String userId) {
        this.userService.setUserAdmin(permissionResource, userId);

    }

    /**
     * Ritorna le linee di cui il principal è amministratore.
     */
    @GetMapping("/admin/lines")
    @ApiOperation("Restituisce le linee di cui è admin il richiedente")
    public List<LineaDTO> getLineAdmin() {
        return this.lineeService.getAllLinesAdminPrincipal();
    }

    /**
     * Ritorna tutti gli utenti guida di cui il principal è amministratore.
     */
    @ApiOperation("Restituisce le guide di cui il richiedente è amministratore")
    @GetMapping("/admin/guides")
    public List<UserInsertResource> getGuideUsers() throws RoleNotFoundException {
        return this.userService.getAllGuidesAdmin();
    }

    /**
     * Ritorna la guida di cui il principal è amministratore partendo dall'email
     */
    @GetMapping("/admin/users")
    @ApiOperation("Ritorna la guida di cui il richiedente è amministratore partendo dall'email")
    public UserInsertResource getGuideUsers(@ApiParam(name = "email", value = "L'ID della guida") @RequestBody String email) {
        return this.userService.getUserByEmail(email);
    }
}
