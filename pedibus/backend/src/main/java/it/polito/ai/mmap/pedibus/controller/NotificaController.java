package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.services.NotificheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
public class NotificaController {


    @Autowired
    NotificheService notificheService;

    /**
     * Restituisce le notifiche non lette di un determinato utente
     */
    @GetMapping("/notifiche/all/{username}")
    public ArrayList<NotificaDTO> getNotifiche(@PathVariable("username") String username){ return notificheService.getNotifiche(username);}

    /**
     * Restituisce le notifiche base non lette di un determinato utente
     */
    @GetMapping("/notifiche/base/{username}")
    public ArrayList<NotificaDTO> getNotificheBase(@PathVariable("username") String username){ return notificheService.getNotificheBase(username);}

    /**
     * Restituisce le notifiche disponibilita non lette di un determinato utente
     */
    @GetMapping("/notifiche/disp/{username}")
    public ArrayList<NotificaDTO> getNotificheDisp(@PathVariable("username") String username){ return notificheService.getNotificheDisponibilita(username);}


//TODO delete

//    @PostMapping("/notifiche/new")
//    public void addNotifica(@RequestBody NotificaDTO notificaDTO){
//        notificheService.addNotifica(new NotificaEntity(notificaDTO));
//    }

    /**
     * Elimina la notifica selezionata da un determinato id
     */
    @DeleteMapping("/notifiche/{idNotifica}")
     public void deleteNotifica(@PathVariable("idNotifica") String idNotifica){ notificheService.deleteNotifica(idNotifica);}
}
