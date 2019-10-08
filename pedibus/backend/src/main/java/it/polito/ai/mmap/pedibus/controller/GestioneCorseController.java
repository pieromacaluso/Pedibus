package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

public class GestioneCorseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    GestioneCorseService gestioneCorseService;

    /**
     * Permette a una guide/admin di una linea di segnalare la propria disponibilità
     *
     * @param idLinea
     * @param verso
     * @param data
     * @throws Exception
     */
    @PostMapping("/disp/{idLinea}/{verso}/{data}")
    public void addDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        gestioneCorseService.addDisp(new DispDTO(principal.getUsername(), new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso)));
    }

    /**
     * Permette a una guide/admin di una linea di annullare la propria disponibilità
     *
     * @param idLinea
     * @param verso
     * @param data
     * @throws Exception
     */
    @DeleteMapping("/disp/{idLinea}/{verso}/{data}")
    public void deleteDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        gestioneCorseService.deleteDisp(new DispDTO(principal.getUsername(), new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso)));
    }


}