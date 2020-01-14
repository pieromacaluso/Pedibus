package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.repository.NotificaRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.resources.DispStateResource;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
     * @param idNotifica
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
     * @param idNotifica
     */
    public void deleteNotificaAdmin(String idNotifica) {
        try {
            NotificaEntity notificaEntity = getNotifica(idNotifica);
            notificaRepository.delete(notificaEntity);
            simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche/trash", notificaEntity);
        } catch (NotificaNotFoundException ignored) {
            return;
        }
    }

    /**
     * Salva nel db una nuova notifica passata come parametro
     */
    public void addNotifica(NotificaEntity notificaEntity) {
        notificaRepository.save(notificaEntity);
        simpMessagingTemplate.convertAndSendToUser(notificaEntity.getUsernameDestinatario(), "/notifiche", notificaEntity);
        logger.info("Notifica salvata e inviata.");
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

    public NotificaEntity generateArrivedNotification(ReservationEntity reservationEntity) {
        UserEntity parent = this.childService.getChildParent(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        return new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato consegnato/a " +
                        (reservationEntity.isVerso() ? "a scuola" : "alla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getArrivatoScuolaDate()), null);
    }

    public NotificaEntity generateHandledNotification(ReservationEntity reservationEntity) {
        UserEntity parent = this.childService.getChildParent(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        return new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato prelevato/a " +
                        (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getPresoInCaricoDate()), null);
    }

    public NotificaEntity generateAssenteNotification(ReservationEntity reservationEntity) {
        UserEntity parent = this.childService.getChildParent(reservationEntity.getCfChild());
        ChildEntity childEntity = this.childService.getChildrenEntity(reservationEntity.getCfChild());
        FermataEntity fermataEntity = this.lineeService.getFermataEntityById(reservationEntity.getIdFermata());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        return new NotificaEntity(NotificaEntity.NotificationType.BASE, parent.getUsername(),
                childEntity.getSurname() + " " + childEntity.getName() + " " + " è stato segnalato/a come assente alla partenza " +
                        (!reservationEntity.isVerso() ? "da scuola" : "dalla fermata " + fermataEntity.getName()) + " alle ore " + simpleDateFormat.format(reservationEntity.getAssenteDate()), null);
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

    public Page<NotificaDTO> getPagedNotifications(String username, Pageable pageable) {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getUsername().equals(username)) {
            Page<NotificaEntity> pagedNotifications = notificaRepository.findAllByUsernameDestinatarioOrderByDataDesc(username, pageable);
           return PageableExecutionUtils.getPage(pagedNotifications.stream().map(NotificaDTO::new).collect(Collectors.toList()), pageable, pagedNotifications::getTotalElements);
        } else {
            throw new UnauthorizedUserException("Operazione non autorizzata");
        }
    }
}
