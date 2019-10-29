package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.resources.*;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.core.AbstractDestinationResolvingMessagingTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO implementare la sicurezza degli endpoint

@RestController
public class GestioneCorseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    GestioneCorseService gestioneCorseService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Permette a una guide/admin di una linea di recuperare la propria disponibilità e le info relative ai turni
     */
    @GetMapping("/disp/{verso}/{data}")
    public DispTurnoResource getDisp(@PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        return gestioneCorseService.getDispTurnoResource(MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso);
    }

    /**
     * Permette a una guide/admin di una linea di segnalare la propria disponibilità
     */
    @PostMapping("/disp/{idLinea}/{verso}/{data}")
    public DispAllResource addDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Integer idFermata) throws Exception {
        DispTurnoResource dispTurnoResource =  gestioneCorseService.addDisp(new DispDTO(idFermata, new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso)));
        simpMessagingTemplate.convertAndSend("/dispws-add/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), dispTurnoResource.getDisp());
        simpMessagingTemplate.convertAndSend("/dispws/" + dispTurnoResource.getDisp().getGuideUsername() + "/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), dispTurnoResource);
        return dispTurnoResource.getDisp();
    }


    /**
     * Permette a una guide/admin di una linea di annullare la propria disponibilità
     */
    @DeleteMapping("/disp/{idLinea}/{verso}/{data}")
    public void deleteDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        DispAllResource deleted = gestioneCorseService.deleteDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
        simpMessagingTemplate.convertAndSend("/dispws/" + deleted.getGuideUsername() + "/" +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), new DispTurnoResource());
        simpMessagingTemplate.convertAndSend("/dispws-del/"  +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), deleted);

    }

    /**
     * Restituisce una list di disponibilità per quel turno
     *
     * @param idLinea
     * @param verso
     * @param data
     * @return
     * @throws Exception
     */
    @GetMapping("/turno/disp/{idLinea}/{verso}/{data}")
    public TurnoDispResource getAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        return gestioneCorseService.getAllTurnoDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
    }

    /**
     * Permette di specificare quali degli accompagnatori è stato confermato
     *
     * @param idLinea
     * @param verso
     * @param data
     * @param dispResourceList
     * @throws Exception
     */
    @PostMapping("/turno/disp/{idLinea}/{verso}/{data}")
    public void setAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody List<DispAllResource> dispResourceList) throws Exception {
        gestioneCorseService.setAllTurnoDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso), dispResourceList);
        for (DispAllResource d : dispResourceList){
            DispStateResource state = new DispStateResource(d);
            simpMessagingTemplate.convertAndSend("/dispws-status/" + d.getGuideUsername() + "/" +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), state);
            simpMessagingTemplate.convertAndSend("/dispws-status/" + "/" +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), d);
        }
    }

    /**
     * Permette alle guide confermate di ack la loro conferma
     *
     * @param idLinea
     * @param verso
     * @param data
     */
    @PostMapping("/turno/disp/ack/{idLinea}/{verso}/{data}")
    public void ackDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        DispAllResource d = gestioneCorseService.ackDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
        DispStateResource state = new DispStateResource(d);
        simpMessagingTemplate.convertAndSend("/dispws-status/" + d.getGuideUsername() + "/" +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), state);
        simpMessagingTemplate.convertAndSend("/dispws-status/" + "/" +data + "/" + idLinea + "/" + ((verso) ? 1 : 0), d);


    }

    /**
     * Permette all'admin di una linea di recuperare lo stato di un turno
     *
     * @param idLinea
     * @param verso
     * @param data
     */
    @GetMapping("/turno/state/{idLinea}/{verso}/{data}")
    public TurnoResource getTurnoState(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        return gestioneCorseService.getTurnoState(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
    }

    /**
     * Permette all'admin di una linea di gestire lo stato di un turno
     *
     * @param idLinea
     * @param verso
     * @param data
     * @param isOpen
     */
    @PutMapping("/turno/state/{idLinea}/{verso}/{data}")
    public void setTurnoState(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Boolean isOpen) {
        TurnoEntity turno = gestioneCorseService.setTurnoState(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso), isOpen);
        TurnoResource tr = new TurnoResource(turno);
        simpMessagingTemplate.convertAndSend("/turnows/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), tr);
    }

}