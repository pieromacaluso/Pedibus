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
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @param username
     * @return
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
     * Restituisce tutte le notifiche non lette dell'utente
     *
     * @param username
     * @return
     */
    //TODO verificare forse non serve più
    /*public ArrayList<NotificaDTO> getNotifiche(String username) {

        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getUsername().equals(username)) {
            List<NotificaEntity> notifiche = notificaRepository.findAllByUsernameDestinatarioOrderByDataDesc(username);
            ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();
            for (NotificaEntity n : notifiche) {
                notificheDTO.add(new NotificaDTO(n));
            }

            return notificheDTO;

        } else {
            throw new UnauthorizedUserException("Operazione non autorizzata");
        }


    }*/


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

    public List<NotificaEntity> generateArrivedNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato consegnato/a " +
                            (reservationEntity.isVerso() ? "a scuola" : "alla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getArrivatoScuolaDate()), null));
        }
        return notificaEntities;
    }

    public List<NotificaEntity> generateHandledNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato prelevato/a " +
                            (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getPresoInCaricoDate()), null));
        }
        return notificaEntities;
    }

    public List<NotificaEntity> generateAssenteNotification(ReservationEntity reservationEntity) {
        List<UserEntity> parents = childService.getChildParents(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<NotificaEntity> notificaEntities = new ArrayList<>();
        for (UserEntity parent : parents) {
            notificaEntities.add(new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                    childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato segnalato/a come assente alla partenza " +
                            (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getAssenteDate()), null));
        }
        return notificaEntities;
    }

    public void sendNotificheDisp(DispEntity dispEntity) {
        DispAllResource d = new DispAllResource(dispEntity,
                lineeService.getFermataEntityById(dispEntity.getIdFermata()),
                lineeService.getLineaEntityById(dispEntity.getIdLinea()));
        Optional<TurnoEntity> turnoCheck = gestioneCorseService.turnoRepository.findByTurnoId(dispEntity.getTurnoId());
        if (turnoCheck.isPresent()) {
            TurnoEntity turnoEntity = turnoCheck.get();
            DispStateResource state = new DispStateResource(d);
            simpMessagingTemplate.convertAndSendToUser(dispEntity.getGuideUsername(), "/dispws-status" + "/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), state);
            simpMessagingTemplate.convertAndSend("/dispws-status/" + "/" + MongoTimeService.dateToString(turnoEntity.getData()) + "/" + turnoEntity.getIdLinea() + "/" + ((turnoEntity.getVerso()) ? 1 : 0), d);
        }
    }

    public NotificaEntity generateDispNotification(DispEntity dispEntity) {
        TurnoEntity turnoEntity = gestioneCorseService.getTurnoEntityById(dispEntity.getTurnoId());
        FermataEntity fermataEntity = lineeService.getFermataEntityById(dispEntity.getIdFermata());
        LineaEntity lineaEntity = lineeService.getLineaEntityById(dispEntity.getIdLinea());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return new NotificaEntity(NotificaEntity.NotificationType.DISPONIBILITA, dispEntity.getGuideUsername(),
                "La tua disponibilità per la corsa di " + (turnoEntity.getVerso() ? "andata" : "ritorno") + " del "
                        + simpleDateFormat.format(turnoEntity.getData()) + " "
                        + (turnoEntity.getVerso() ? "con partenza d" : "con arrivo ")
                        + "alla fermata " + fermataEntity.getName() + " della " + lineaEntity.getNome()
                        + " alle ore " + fermataEntity.getOrario() + " è stata confermata", dispEntity.getDispId());
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

        if (roleEntitySys.isPresent()) {
            Optional<List<UserEntity>> userEntities = this.userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(roleEntitySys.get());
            if (userEntities.isPresent()) {
                for (UserEntity admin : userEntities.get())
                    this.simpMessagingTemplate.convertAndSendToUser(admin.getUsername(), "/anagrafica", "updates");
            }
        }
        if (roleEntityAdmin.isPresent()) {
            Optional<List<UserEntity>> userEntities = this.userRepository.findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(roleEntityAdmin.get());
            if (userEntities.isPresent()) {
                for (UserEntity admin : userEntities.get())
                    this.simpMessagingTemplate.convertAndSendToUser(admin.getUsername(), "/anagrafica", "updates");
            }
        }
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
}
