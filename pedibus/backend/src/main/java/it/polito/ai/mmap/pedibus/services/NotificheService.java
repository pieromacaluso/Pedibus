package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.exception.UserNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.repository.NotificaRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import lombok.Data;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data

public class NotificheService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NotificaRepository notificaRepository;

    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;

    @Autowired
    UserRepository userRepository;


    /**
     * Restituisce le notifiche non lette con type
     *
     * @param username
     * @return
     */
    public ArrayList<NotificaDTO> getNotificheType(String username,String type) {
        if(type.equals(NotBASE) || type.equals(NotDISPONIBILITA)){
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal.getUsername()==username){
                List<NotificaEntity> notifiche = notificaRepository.findAllByUsernameDestinatarioAndIsAckAndIsTouchedAndAndType(username,false,false,type);
                ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();

                for (NotificaEntity n : notifiche) {
                    notificheDTO.add(new NotificaDTO(n));
                }

                return notificheDTO;
            }else{
                throw new UnauthorizedUserException("Operazione non autorizzata");
            }
        }else{
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
        if(principal.getUsername().equals(username)){
            List<NotificaEntity> notifiche = notificaRepository.findAllByUsernameDestinatarioAndIsAckAndIsTouched(username,false,false);
            ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();
            for (NotificaEntity n : notifiche) {
                notificheDTO.add(new NotificaDTO(n));
            }

            return notificheDTO;

        }else{
            throw new UnauthorizedUserException("Operazione non autorizzata");
        }



    }


    /**
     * Elimina una qualsiasi notifica attraverso il suo id
     *
     * @param idNotifica
     */
    public void deleteNotifica(String idNotifica) {
        Optional<NotificaEntity> checkNotificaEntity = notificaRepository.findById(idNotifica);
        if (checkNotificaEntity.isPresent()) {
            //idNotifica di una notifica base
            NotificaEntity notificaEntity = checkNotificaEntity.get();
            if (notificaEntity.getType().equals(NotBASE))
                notificaRepository.delete(notificaEntity);
        } else {
            throw new NotificaNotFoundException();
        }
    }

    /**
     * Salva nel db una nuova notifica passata come parametro
     */
    public void addNotifica(NotificaEntity notificaEntity) {
        notificaRepository.save(notificaEntity);
        logger.info("Notifica salvata.");
    }

}
