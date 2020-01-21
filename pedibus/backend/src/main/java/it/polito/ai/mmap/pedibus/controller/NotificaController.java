package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import it.polito.ai.mmap.pedibus.services.NotificheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class NotificaController {

    @Autowired
    NotificheService notificheService;

    /**
     * Restituisce le notifiche non lette di un determinato utente
     *
     * @param username username dell'utente
     * @param pageable struttura per paginazione
     * @return Pagina di notifiche
     */
    @GetMapping("/notifiche/all/{username}")
    public Page<NotificaDTO> getPagedNotifications(@PathVariable("username") String username, Pageable pageable) {
        return notificheService.getPagedNotifications(username, pageable);
    }

    /**
     * Elimina la notifica selezionata da un determinato id
     *
     * @param idNotifica id della notifica da eliminare
     */
    @DeleteMapping("/notifiche/{idNotifica}")
    public void deleteNotifica(@PathVariable("idNotifica") String idNotifica) {
        notificheService.deleteNotifica(idNotifica);
    }
}
