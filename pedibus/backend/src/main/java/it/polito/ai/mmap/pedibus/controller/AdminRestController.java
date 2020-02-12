package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.SysAdminException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.resources.*;
import it.polito.ai.mmap.pedibus.services.*;
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
import java.util.Date;
import java.util.List;

@RestController
@ApiOperation("Endpoint dedicati al sysadmin e admin")
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
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private GestioneCorseService gestioneCorseService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private MongoTimeService mongoTimeService;
    @Autowired
    private NotificheService notificheService;

    /**
     * Endpoint per ottenere tutti i ruoli disponibili nel database
     *
     * @return Lista di Ruoli sottoforma di stringhe da assegnare agli utenti
     */
    @GetMapping("/sysadmin/roles")
    @ApiOperation("Restituisce tutti i ruoli disponibili sul database")
    public List<String> getAvailableRole() {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/sysadmin/roles"));
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
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/sysadmin/users?heyword=" + keyword));
        return userService.getAllPagedUsers(pageable, keyword);
    }

    /**
     * Aggiunta di un nuovo utente al database da parte di un amministratore. Questo endpoint genererà un token
     * che verrà inviato all'utente, il quale potrà procedere con il cambio password e l'abilitazione dell'utente.
     *
     * @param userInsertResource DTO per inserimento Utente da parte dell'amministratore
     * @param bindingResult      contiene i risultati del processo di validazione
     * @return UserEntity dell'utente inserito correttamente
     */
    @PostMapping("/sysadmin/users")
    @ApiOperation("Aggiunge un nuovo utente al database, inviando una mail per attivare l'account")
    public UserEntity insertUser(@ApiParam(name = "userInsertResource", value = "Contiene i dettagli dell'utente che verrà inserito") @Valid @RequestBody UserInsertResource userInsertResource,
                                 BindingResult bindingResult) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/sysadmin/users " + userInsertResource.getUserId()));
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_USER_ERROR);
        }
        return userService.insertUser(userInsertResource);
    }

    /**
     * Modifica i dati di un utente già creato, da parte dell'amministratore.
     *
     * @param userId             id dell'utente
     * @param userInsertResource l'utente i cui dettagli vanno aggiornati
     * @param bindingResult      contiene i risultati della validazione
     */
    @PutMapping("/sysadmin/users/{userId}")
    @ApiOperation("Modifica i dati di un utente già creato")
    public void updateUser(@ApiParam(name = "userId", value = "L'ID dell'utente da modificare") @PathVariable("userId") String userId,
                           @ApiParam(name = "userInsertResource", value = "Contiene i dati aggiornati dell'utente") @Valid @RequestBody UserInsertResource userInsertResource,
                           BindingResult bindingResult) {
        logger.info(PedibusString.ENDPOINT_CALLED("PUT", "/sysadmin/users/" + userId));
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
        logger.info(PedibusString.ENDPOINT_CALLED("DELETE", "/sysadmin/users/" + userId));
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
    public Page<ChildDTO> getChildren(Pageable pageable,
                                      @ApiParam(name = "keyword", value = "Filtra gli user restituiti. Possono essere più di una separate da spazi")
                                      @RequestParam("keyword") String keyword) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/sysadmin/children?keyword=" + keyword));
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
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/sysadmin/children/" + cfChild));
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
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/sysadmin/children " + childDTO.getCodiceFiscale()));
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.POST_CHILD_ERROR);
        }
        childDTO.setCodiceFiscale(childDTO.getCodiceFiscale().toUpperCase());
        ChildEntity childEntity = childService.createChild(childDTO);
        return new ChildDTO(childEntity);
    }

    /**
     * Modifica di un bambino sul database da parte di un amministratore.
     *
     * @param childId       id del bambino da modificare
     * @param childDTO      Dati del bambino da modificare
     * @param bindingResult validazione oggetto
     * @return ChildDTO del bambino modificato
     */
    @PutMapping("/sysadmin/children/{childId}")
    @ApiOperation("Modifica i dati di un bambino già creato")
    public ChildDTO updateChild(@ApiParam(name = "childId", value = "Il codice fiscale del bambino da modificare") @PathVariable("childId") String childId,
                                @ApiParam(name = "childDTO", value = "Contiene i dati aggiornati del bambino") @Valid @RequestBody ChildDTO childDTO,
                                BindingResult bindingResult) {
        logger.info(PedibusString.ENDPOINT_CALLED("PUT", "/sysadmin/children/" + childId));
        if (bindingResult.hasErrors()) {
            throw new SysAdminException(PedibusString.PUT_CHILD_ERROR);
        }
        childDTO.setCodiceFiscale(childDTO.getCodiceFiscale().toUpperCase());
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
        logger.info(PedibusString.ENDPOINT_CALLED("DELETE", "/sysadmin/children/" + childId));
        childService.deleteChild(childId);
    }

    /**
     * Un admin di una linea o il system-admin inserisce un utente come admin per una linea, indicando
     * tramite PermissionResource.addOrDel se aggiungere(true) o eliminare(false) il permesso
     *
     * @param permissionResource Linea e booleano
     * @param userId             L'id dell'utente
     */
    @PutMapping("/admin/users/{userID}")
    @ApiOperation("Aggiunge o rimuove il ruolo di admin per un utente")
    public void setUserAdmin(@ApiParam(name = "permissionResource", value = "Contiene i dettagli dei permessi da assegnare al dato utente") @RequestBody PermissionResource permissionResource,
                             @ApiParam(name = "userId", value = "L'ID dell'utente") @PathVariable("userID") String userId) {
        logger.info(PedibusString.ENDPOINT_CALLED("PUT", "/admin/users/" + userId));
        this.userService.setUserAdmin(permissionResource, userId);

    }

    /**
     * @return le linee di cui il principal è amministratore.
     */
    @GetMapping("/admin/lines")
    @ApiOperation("Restituisce le linee di cui è admin il richiedente")
    public List<LineaDTO> getLineAdmin() {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/admin/lines"));
        return this.lineeService.getAllLinesAdminPrincipal();
    }

    /**
     * @return gli utenti che possono essere resi amministratori di linea
     * @throws RoleNotFoundException errore di sistema
     */
    @ApiOperation("Restituisce gli utenti che possono essere resi amministratori di linea")
    @GetMapping("/admin/users")
    public List<UserInsertResource> getGuideUsers() throws RoleNotFoundException {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/admin/users"));
        return this.userService.getAllGuidesAdmin();
    }

    /**
     * Permette a un amministratore di linea di annullare una  disponibilità
     *
     * @param idDisp id disponibilità
     */
    @DeleteMapping("admin/disp/{idDisp}")
    @ApiOperation("Permette di annullare una disponibilità")
    public void deleteDispAdmin(@PathVariable("idDisp") String idDisp) {
        logger.info(PedibusString.ENDPOINT_CALLED("DELETE", "/disp/" + idDisp));
        gestioneCorseService.deleteAdminDisp(idDisp);
    }

    /**
     * Permette a una guide di aggiornare la propria disponibilità
     *
     * @param idDisp id disponibilità
     * @param disp   Dati sulla disponibilità da aggiornare
     * @return Disponibilità aggiornata
     */
    @PutMapping("/admin/disp/{idDisp}")
    @ApiOperation("Permette di aggiornare una disponibilità")
    public DispAllResource updateDisp(@PathVariable("idDisp") String idDisp, @RequestBody DispAllResource disp) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/disp/" + idDisp));
        DispTurnoResource res = gestioneCorseService.updateDisp(idDisp, disp);
        return res.getDisp();
    }

    /**
     * Restituisce il dump delle reservations per la terna indicata
     *
     * @param idLinea id della linea
     * @param data    data in esame
     * @param verso   verso considerato
     * @return ReservationsDump
     */
    @ApiOperation("Restituisce il dump delle reservations per la terna linea/data/verso indicata")
    @GetMapping("/admin/reservations/dump/{id_linea}/{data}/{verso}")
    public ReservationsDump getReservationsDump(@PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/reservations/dump/" + idLinea + "/" + data + "/" + verso));
        return reservationService.getReservationsDump(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, false), verso);
    }

    /**
     * Elimina la reservation indicata. Funzione utilizzabile solo dal systemadmin
     *
     * @param data    data
     * @param verso   verso
     * @param cfChild codice fiscale bambino
     */
    @DeleteMapping("/sysadmin/reservations/{data}/{verso}/{cfChild}")
    public void deleteReservation(@PathVariable("data") String data, @PathVariable("verso") boolean verso, @PathVariable("cfChild") String cfChild) {
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data, true);
        logger.info(PedibusString.ENDPOINT_CALLED("DELETE", "/reservations/" + data + "/" + verso + "/" + cfChild));
        ReservationEntity reservationEntity = reservationService.getChildReservation(verso, dataFormatted, cfChild);
        reservationService.deleteReservation(reservationEntity.getIdLinea(), dataFormatted, reservationEntity.getId());
        this.notificheService.sendReservationNotification(new ReservationDTO(reservationEntity), true);
        this.notificheService.generateDeletedReservation(reservationEntity);
    }
}
