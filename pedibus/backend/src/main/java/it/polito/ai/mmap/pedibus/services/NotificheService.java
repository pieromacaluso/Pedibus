package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.repository.NotificaRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public ArrayList<NotificaDTO> getNotifiche(String username) {

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


    }


    /**
     * Elimina una qualsiasi notifica attraverso il suo id
     *
     * @param idNotifica
     */
    public void deleteNotifica(String idNotifica) {
            NotificaEntity notificaEntity = getNotifica(idNotifica);
            notificaRepository.delete(notificaEntity);
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
     * @param idNotifica
     * @return
     * @throws NotificaNotFoundException
     */
    public NotificaEntity getNotifica(String idNotifica) throws NotificaNotFoundException{
        Optional<NotificaEntity> checkNotificaEntity = notificaRepository.findById(idNotifica);
        if(checkNotificaEntity.isPresent()){
            return checkNotificaEntity.get();
        }else
            throw new NotificaNotFoundException();
    }
}
