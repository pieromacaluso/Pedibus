package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.NotificaAckEntity;
import it.polito.ai.mmap.pedibus.entity.NotificaBaseEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.UserNotFoundException;
import it.polito.ai.mmap.pedibus.repository.NotificaAckRepository;
import it.polito.ai.mmap.pedibus.repository.NotificaBaseRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.resources.NotificaResource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data

public class NotificheService {
    @Autowired
    NotificaAckRepository notificaAckRepository;
    @Autowired
    NotificaBaseRepository notificaBaseRepository;
    @Autowired
    UserRepository userRepository;

    /**
     * Restituisce le notifiche dell'utente specificato sotto forma di NotificaResource
     * @param username
     * @return
     */
    public ArrayList<NotificaResource> getNotificheBase(String username) {
        Optional<UserEntity> user=userRepository.findByUsername(username);
        if(user.isPresent()){
            //todo verifica se bisogna filtrare per ogni utente solo le notifiche corrispondenti (Es. solo per la linea di cui fa parte l'utente, in teoria non dovrebbero esserci notifiche per linee diverse dalla sua, pero magari qualcosa del passato se poi ha cambiato linea, e altri casi da vedere bene)
            List<NotificaBaseEntity> notifiche = notificaBaseRepository.findAll().stream().filter(notificaBaseEntity -> notificaBaseEntity.getUsernameDestinatario().equals(username)).filter(notificaBaseEntity -> !notificaBaseEntity.getIsTouched()).collect(Collectors.toList());
            ArrayList<NotificaResource> notificheResources = new ArrayList<>();

            for(NotificaBaseEntity n:notifiche){
                NotificaResource notRes=new NotificaResource(n.getIdNotifica(),n.getMsg());
                notificheResources.add(notRes);
            }

            return notificheResources;
        }else{
            throw new UserNotFoundException();
        }

    }

    /**
     * Elimina una qualsiasi notifica attraverso il suo id
     * @param idNotifica
     */
    public void deleteNotifica(String idNotifica) {
        Optional<NotificaBaseEntity> checkNotificaBaseEntity=notificaBaseRepository.findById(idNotifica);
        if(checkNotificaBaseEntity.isPresent()){
            //idNotifica di una notifica base
            notificaBaseRepository.delete(checkNotificaBaseEntity.get());
        }else{
            Optional<NotificaAckEntity> checkNotificaAckEntity=notificaAckRepository.findById(idNotifica);
            if(checkNotificaAckEntity.isPresent()){
                //dNotifica di una notifica Ack
                notificaAckRepository.delete(checkNotificaAckEntity.get());
            }else{
                //no base e no ank
                throw new NotificaNotFoundException();
            }
        }
    }
}
