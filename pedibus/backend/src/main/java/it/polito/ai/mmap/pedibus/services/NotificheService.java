package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.repository.NotificaRepository;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.resources.DispStateResource;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import lombok.Data;
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
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data

public class NotificheService {
    @Autowired
    NotificaRepository notificaRepository;
    @Autowired
    UserRepository userRepository;
    @Value("${arrivoScuola}")
    String arrivoScuola;
    @Value("${partenzaScuola}")
    String partenzaScuola;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private GestioneCorseService gestioneCorseService;
    @Autowired
    private ChildService childService;
    @Autowired
    private LineeService lineeService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MongoTimeService mongoTimeService;

    /**
     * Restituisce le notifiche non lette con type
     *
     * @param username email destinatario
     * @param type tipo di notifica voluta
     */
    public ArrayList<NotificaDTO> getNotificheType(String username, NotificaEntity.NotificationType type) {
        if (type.compareTo(NotificaEntity.NotificationType.BASE) == 0 || type.compareTo(NotificaEntity.NotificationType.DISPONIBILITA) == 0) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.getUsername().equals(username)) {
                List<NotificaEntity> notifiche = notificaRepository.findAllByUsernameDestinatarioAndIsAckAndIsTouchedAndAndType(username, false, false, type);
                ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();

                for (NotificaEntity n : notifiche) {
                    notificheDTO.add(new NotificaDTO(n));
                }

                return notificheDTO;
            } else {
                throw new UnauthorizedUserException("Operazione non autorizzata");
            }
        } else {
            throw new NotificaWrongTypeException();
        }

    }
    
    /**
     * Elimina una notifica attraverso il suo id. Solo il destinatario di una determinata notifica può eliminarla
     *
     * @param idNotifica id Notifica da Eliminare
     */
    public void deleteNotifica(String idNotifica) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificaEntity notificaEntity = getNotifica(idNotifica);
        if (notificaEntity.getUsernameDestinatario().compareTo(principal.getUsername()) != 0)
            throw new IllegalArgumentException();
        if (notificaEntity.getType().equals(NotificaEntity.NotificationType.DISPONIBILITA)) {
            gestioneCorseService.setAckDisp(notificaEntity.getDispID());
        }
        notificaRepository.delete(notificaEntity);
        simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche/trash", notificaEntity);

    }

    /**
     * Elimina una notifica attraverso il suo id. Metodo utilizzato per eliminare una notifica indesiderata mandata da un amministratore o guida.
     *
     * @param idNotifiche elenco delle notifiche da eliminare
     */
    public void deleteNotificaAdmin(List<String> idNotifiche) {
        try {
            for (String idNotifica : idNotifiche) {
                NotificaEntity notificaEntity = getNotifica(idNotifica);
                notificaRepository.delete(notificaEntity);
                simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche/trash", notificaEntity);
            }
        } catch (NotificaNotFoundException ignored) {
            return;
        }
    }

    /**
     * Salva nel db una nuova notifica passata come parametro
     *
     * @param notificaEntity notifica
     */
    public void addNotifica(NotificaEntity notificaEntity) {
        NotificaEntity notificaEntity1 = notificaRepository.save(notificaEntity);
        simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche", notificaEntity);
        logger.info(PedibusString.NOTIFICATION_SENT);
    }

    /**
     * Ricevuto un id di una notifica ritorna la notifica corrispondente se presente altrimente lancia una eccezione
     *
     * @param idNotifica
     * @return
     * @throws NotificaNotFoundException
     */
    public NotificaEntity getNotifica(String idNotifica) throws NotificaNotFoundException {
        Optional<NotificaEntity> checkNotificaEntity = notificaRepository.findById(idNotifica);
        if (checkNotificaEntity.isPresent()) {
            return checkNotificaEntity.get();
        } else
            throw new NotificaNotFoundException();
    }

    /**
     * Permette di inviare una notifica quando un bambino è stato preso in carico da un pedibus
     * @param reservationEntity Entity della prenotazione del bambino specificato
     */
    public List<NotificaEntity> generateHandledNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat hFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato/a preso/a in carico " +
                            (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + (!reservationEntity.isVerso() ? partenzaScuola : fermataEntity.getOrario()) +
                            " del " + dateFormat.format(reservationEntity.getData()), null));
        }
        return notificaEntities;
    }

    /**
     * Permette di inviare una notifica quando un bambino è arrivato a scuola o alla fermata di ritorno
     * @param reservationEntity Entity della prenotazione del bambino specificato
     */
    public List<NotificaEntity> generateArrivedNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat hFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è arrivato/a " +
                            (reservationEntity.isVerso() ? "a scuola" : "alla fermata " + fermataEntity.getName()) + " alle ore " + (reservationEntity.isVerso() ? arrivoScuola : fermataEntity.getOrario()) +
                            " del " + dateFormat.format(reservationEntity.getData()), null));
        }
        return notificaEntities;
    }


    /**
     * Permette di inviare una notifica quando un bambino è assente ad una fermata
     * @param reservationEntity Entity della prenotazione del bambino specificato
     */
    public List<NotificaEntity> generateAssenteNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat hFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato segnalato/a come assente alla partenza " +
                            (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + (!reservationEntity.isVerso() ? partenzaScuola : fermataEntity.getOrario()) +
                            " del " + dateFormat.format(reservationEntity.getData()), null));
        }
        return notificaEntities;
    }

    /**
     * Gestione webSocket per una disponibilità
     * @param dispEntity disponibilità in questione
     */
    public void sendNotificheDisp(DispEntity dispEntity) {
        DispAllResource d = new DispAllResource(dispEntity,
                lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        Optional<TurnoEntity> turnoCheck = gestioneCorseService.turnoRepository.findByTurnoId(dispEntity.getTurnoId());
        if (turnoCheck.isPresent()) {
            TurnoEntity turnoEntity = turnoCheck.get();
            DispStateResource state = new DispStateResource(d);
            simpMessagingTemplate.convertAndSendToUser(dispEntity.getGuideUsername(), "/dispws/status" + "/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), state);
            simpMessagingTemplate.convertAndSend("/dispws/status/" + "/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), d);
        }
    }

    /**
     * Crea una notifica per avvisare un accompagnatore che la sua disponibilità è stata confermata
     * @param dispEntity disponibilità in questione
     */
    public NotificaEntity generateDispNotification(DispEntity dispEntity) {
        TurnoEntity turnoEntity = gestioneCorseService.getTurnoEntityById(dispEntity.getTurnoId());
        FermataEntity fermataEntity = lineeService.getFermataEntityById(dispEntity.getIdFermata());
        LineaEntity lineaEntity = lineeService.getLineaEntityById(dispEntity.getIdLinea());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return new NotificaEntity(NotificaEntity.NotificationType.DISPONIBILITA, dispEntity.getGuideUsername(),
                "La tua disponibilità per il " + simpleDateFormat.format(turnoEntity.getData())
                        + (turnoEntity.getVerso() ? " dalla " : " fino alla ") + "fermata " + fermataEntity.getName()
                        + " (" + lineaEntity.getNome() + " - " + fermataEntity.getOrario() + ") è stata confermata."
                , dispEntity.getDispId());
    }

    /**
     * Restituisce tutte le notifiche non lette di un determinato utente utilizzando paginazione
     *
     * @param username username dell'utente
     * @param pageable oggeto paginazione
     * @return Pagina di notifiche richiesta
     */
    public Page<NotificaDTO> getPagedNotifications(String username, Pageable pageable) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getUsername().equals(username)) {
            Page<NotificaEntity> pagedNotifications = notificaRepository.findAllByUsernameDestinatarioOrderByDataDesc(username, pageable);
            return PageableExecutionUtils.getPage(pagedNotifications.stream().map(NotificaDTO::new).collect(Collectors.toList()), pageable, pagedNotifications::getTotalElements);
        } else {
            throw new UnauthorizedUserException(PedibusString.UNAUTHORIZED_OPERATION);
        }
    }

    /**
     * Funzione che si occupa di spedire le notifiche a tutti gli amministratori di sistema che sono avvenuti cambiamenti
     * nel pannello di anagrafica ed è quindi necessario ricaricarlo.
     */
    public void sendUpdateNotification() {
        Optional<RoleEntity> roleEntitySys = this.roleRepository.findById("ROLE_SYSTEM-ADMIN");
        Optional<RoleEntity> roleEntityAdmin = this.roleRepository.findById("ROLE_ADMIN");

        roleEntitySys.ifPresent(sysRole -> userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(sysRole).forEach(sysEntity -> this.simpMessagingTemplate.convertAndSendToUser(sysEntity.getUsername(), "/anagrafica", "updates")));
        roleEntityAdmin.ifPresent(adminRole -> userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(adminRole).forEach(adminEntity -> this.simpMessagingTemplate.convertAndSendToUser(adminEntity.getUsername(), "/anagrafica", "updates")));
    }

    /**
     * Invio notifiche riguardanti cambiamento prenotazione a chi è interessato. Se `delete` viene settato a true,
     * la notifica inviata avrà l'id della fermata settato a null, quindi si avrà la cancellazione della prenotazione
     * lato frontend
     *
     * @param res    ReservationDTO della prenotazione
     * @param delete true se la prenotazione è da contrassegnare come eliminata, false se no
     */
    public void sendReservationNotification(ReservationDTO res, boolean delete) {
        ReservationResource reservationResource = ReservationResource.builder()
                .cfChild(res.getCfChild()).idFermata(res.getIdFermata()).verso(res.getVerso()).build();
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(res.getIdFermata());
        String data = MongoTimeService.dateToString(res.getData());
        if (delete) res.setIdFermata(null);
        simpMessagingTemplate.convertAndSend("/reservation/" + data +
                "/" + fermataEntity.getIdLinea() + "/" + ((res.getVerso()) ? 1 : 0), res);
        List<UserEntity> parents = childService.getChildParents(res.getCfChild());
        for (UserEntity parent : parents) {
            simpMessagingTemplate.convertAndSendToUser(parent.getUsername(), "/child/res/" +
                    reservationResource.getCfChild() + "/" + data, res);
        }

    }

    /**
     * cancella una notifica riguardante una disponibilità dato l'id della disponibilità stessa
     * @param dispId id disponibilità
     */
    public void deleteNotificaDisp(ObjectId dispId) {
        Optional<NotificaEntity> notificaCheck = this.notificaRepository.findByDispID(dispId);
        if (notificaCheck.isPresent()) {
            NotificaEntity notificaEntity = notificaCheck.get();
            this.notificaRepository.deleteByDispID(dispId);
            simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche/trash", notificaCheck);
        }
    }

    /**
     * Genera una notifica per una guida che è stata promossa ad amministratore di linea oppure per un amministratore di linea che è stato declassato a guida semplce
     * @param userId email guida
     * @param permissionResource permessi associati
     */
    public void generatePromotionNotification(String userId, PermissionResource permissionResource) {
        NotificaEntity notifica = new NotificaEntity(NotificaEntity.NotificationType.BASE, userId, (permissionResource.isAddOrDel() ? "Sei stato promosso ad " : "Ti sono stati revocati i privilegi di ") + "amministratore per la "
                + this.lineeService.getLineaEntityById(permissionResource.getIdLinea()).getNome() + ". Si prega di effettuare nuovamente il login.", null);
        this.notificaRepository.save(notifica);
        simpMessagingTemplate.convertAndSendToUser(notifica.getUsernameDestinatario(), "/notifiche", notifica);
    }

    /**
     * Invia notifica per indicare che la disponibilità di una guida riferita ad un determinato turno non è necessaria
     * @param d info della disponibilità
     * @param turnoEntity turno in questione
     */
    public void generateNotUsedDisp(DispAllResource d, TurnoEntity turnoEntity) {
        String data = MongoTimeService.dateToString(turnoEntity.getData());
        NotificaEntity notifica = new NotificaEntity(NotificaEntity.NotificationType.BASE, d.getGuideUsername(), "La tua disponibilità per " + (turnoEntity.getVerso() ? "l'andata" : "il ritorno") + " in data " + data + " non è necessaria.", null);
        this.notificaRepository.save(notifica);
        simpMessagingTemplate.convertAndSendToUser(notifica.getUsernameDestinatario(), "/notifiche", notifica);
    }
}
