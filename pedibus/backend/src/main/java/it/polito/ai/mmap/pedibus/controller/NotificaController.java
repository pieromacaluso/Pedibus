package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.services.NotificheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/notifiche/paged/{username}")
    public PageImpl<NotificaDTO> getPagedNotifications(@PathVariable("username") String username, Pageable pageable){ return notificheService.getPagedNotifications(username, pageable);}

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
