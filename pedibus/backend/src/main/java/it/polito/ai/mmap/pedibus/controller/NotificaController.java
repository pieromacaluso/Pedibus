package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.resources.NotificaResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;



public class NotificaController {

    /**
     * Restituisce le notifiche non lette di un determinato utente
     */
    @GetMapping("/notifiche/{username}")
    ArrayList<NotificaResource> getNotifiche(){

        return new ArrayList<NotificaResource>();
    }

    /**
     * Elimina la notifica selezionata per un determinato utente
     */
    @DeleteMapping("/notifiche/{idNotifica}")
     void deleteNotifiche(){

    }
}
