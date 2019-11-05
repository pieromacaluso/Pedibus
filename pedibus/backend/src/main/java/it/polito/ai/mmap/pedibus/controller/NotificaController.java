package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.resources.NotificaResource;
import it.polito.ai.mmap.pedibus.services.NotificheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;


@RestController
public class NotificaController {

    //todo gestire permessi ruoli notifiche varie

    @Autowired
    NotificheService notificheService;

    /**
     * Restituisce le notifiche non lette di un determinato utente
     */
    @GetMapping("/notifiche/{username}")
    public ArrayList<NotificaDTO> getNotifiche(@PathVariable("username") String username){ return notificheService.getNotifiche(username);}

    /**
     * Restituisce le notifiche base non lette di un determinato utente
     */
    @GetMapping("/notificheBase/{username}")
    public ArrayList<NotificaDTO> getNotificheBase(@PathVariable("username") String username){ return notificheService.getNotificheBase(username);}

    /**
     * Restituisce le notifiche disponibilita non lette di un determinato utente
     */
    @GetMapping("/notificheDisp/{username}")
    public ArrayList<NotificaDTO> getNotificheDisp(@PathVariable("username") String username){ return notificheService.getNotificheDisponibilita(username);}



    /**
     * Elimina la notifica selezionata da un determinato id
     */
    @DeleteMapping("/notifiche/{idNotifica}")
     public void deleteNotifica(@PathVariable("idNotifica") String idNotifica){ notificheService.deleteNotifica(idNotifica);}
}
