package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.NotificaAckEntity;
import it.polito.ai.mmap.pedibus.entity.NotificaBaseEntity;
import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaNotFoundException;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.exception.UserNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.repository.NotificaAckRepository;
import it.polito.ai.mmap.pedibus.repository.NotificaBaseRepository;
import it.polito.ai.mmap.pedibus.repository.NotificaRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.resources.NotificaResource;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data

public class NotificheService {

    @Autowired
    NotificaRepository notificaRepository;

    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;

    @Autowired
    UserRepository userRepository;

    /**
     * Restituisce le notifiche non lette con type 'base' dell'utente
     * @param username
     * @return
     */
    public ArrayList<NotificaDTO> getNotificheBase(String username) {
        Optional<UserEntity> user=userRepository.findByUsername(username);
        if(user.isPresent()){
            List<NotificaEntity> notifiche = notificaRepository.findAll().stream().filter(notificaEntity -> notificaEntity.getUsernameDestinatario().equals(username)).filter(notificaEntity -> notificaEntity.getType().compareTo(NotBASE)==0).filter(notificaEntity -> !notificaEntity.getIsTouched()).collect(Collectors.toList());
            ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();

            for(NotificaEntity n:notifiche){
                notificheDTO.add(new NotificaDTO(n));
            }

            return notificheDTO;
        }else{
            throw new UserNotFoundException();
        }
    }

    /**
     * Restituisce le notifiche non lette e non ack con type 'disponibilita' dell'utente
     * @param username
     * @return
     */
    public ArrayList<NotificaDTO> getNotificheDisponibilita(String username) {
        Optional<UserEntity> user=userRepository.findByUsername(username);
        if(user.isPresent()){
            List<NotificaEntity> notifiche = notificaRepository.findAll().stream().filter(notificaEntity -> notificaEntity.getUsernameDestinatario().equals(username)).filter(notificaEntity -> notificaEntity.getType().compareTo(NotDISPONIBILITA)==0).filter(notificaEntity -> !notificaEntity.getIsTouched()).filter(notificaEntity -> !notificaEntity.getIsAck()).collect(Collectors.toList());
            ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();

            for(NotificaEntity n:notifiche){
                notificheDTO.add(new NotificaDTO(n));
            }

            return notificheDTO;
        }else{
            throw new UserNotFoundException();
        }
    }

    /**
     * Restituisce tutte le notifiche non lette dell'utente
     * @param username
     * @return
     */
    public ArrayList<NotificaDTO> getNotifiche(String username) {
        Optional<UserEntity> user=userRepository.findByUsername(username);
        if(user.isPresent()){
            List<NotificaEntity> notifiche = notificaRepository.findAll().stream().filter(notificaEntity -> notificaEntity.getUsernameDestinatario().equals(username)).filter(notificaEntity -> !notificaEntity.getIsTouched()).filter(notificaEntity -> !notificaEntity.getIsAck()).collect(Collectors.toList());
            ArrayList<NotificaDTO> notificheDTO = new ArrayList<>();

            for(NotificaEntity n:notifiche){
                notificheDTO.add(new NotificaDTO(n));
            }

            return notificheDTO;
        }else{
            throw new UserNotFoundException();
        }
    }




    /**
     * Elimina una qualsiasi notifica attraverso il suo id
     * @param idNotifica
     */
    public void deleteNotifica(String idNotifica) {
        Optional<NotificaEntity> checkNotificaEntity=notificaRepository.findById(idNotifica);
        if(checkNotificaEntity.isPresent()){
            //idNotifica di una notifica base
            notificaRepository.delete(checkNotificaEntity.get());
        }else{
                throw new NotificaNotFoundException();
        }
    }

    /**
     * Salva nel db una nuova notifica base creata con i parametri passato al metodo.
     * Comodo avere un metodo separato essendo quelle più comuni, evitando ogni volta di passare il tipo e altri parametri non usati
     * @param user
     * @param msg
     * @param isTouched
     */
    public void addNotificaBase(String user,String msg,Boolean isTouched){
        NotificaEntity notificaEntity=new NotificaEntity(NotBASE,user,msg,isTouched,null,false);
        notificaRepository.save(notificaEntity);
    }

    /**
     * Salva nel db una nuova notifica creata con i parametri passato al metodo
     * @param type
     * @param user
     * @param msg
     * @param isTouched
     * @param dispID
     * @param isAck
     */
    public void addNotifica(String type, String user, String msg, Boolean isTouched, ObjectId dispID, Boolean isAck){
        NotificaEntity notificaEntity;
        if(type.compareTo(NotBASE)==0){
            notificaEntity=new NotificaEntity(NotBASE,user,msg,isTouched,null,false);
        }else if(type.compareTo(NotDISPONIBILITA)==0){
            notificaEntity=new NotificaEntity(NotDISPONIBILITA,user,msg,isTouched,dispID,isAck);
        }else{
            throw new NotificaWrongTypeException();
        }

        notificaRepository.save(notificaEntity);
    }
}
