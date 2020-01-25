package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
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
    @ApiOperation("Restituisce le notifiche non lette di un determinato utente paginandole")
    public Page<NotificaDTO> getPagedNotifications(@PathVariable("username") String username, Pageable pageable) {
        return notificheService.getPagedNotifications(username, pageable);
    }

    /**
     * Elimina la notifica indicata
     *
     * @param idNotifica id della notifica da eliminare
     */
    @DeleteMapping("/notifiche/{idNotifica}")
    @ApiOperation("Elimina la notifica indicata")
    public void deleteNotifica(@PathVariable("idNotifica") String idNotifica) {
        notificheService.deleteNotifica(idNotifica);
    }
}
