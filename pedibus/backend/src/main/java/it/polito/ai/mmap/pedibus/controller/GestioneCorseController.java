package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.resources.*;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

//TODO implementare la sicurezza degli endpoint

@RestController
public class GestioneCorseController {
    @Autowired
    GestioneCorseService gestioneCorseService;
    @Autowired
    MongoTimeService mongoTimeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Permette a una guide/admin di una linea di recuperare la propria disponibilità e e lo stato del relativo turno
     */
    @GetMapping("/disp/{verso}/{data}")
    public DispTurnoResource getDisp(@PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
            return gestioneCorseService.getDispTurnoResource(mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso);
    }

    /**
     * Permette a una guide di segnalare la propria disponibilità
     */
    @PostMapping("/disp/{idLinea}/{verso}/{data}")
    public DispAllResource addDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Integer idFermata) throws Exception {
        DispTurnoResource dispTurnoResource = gestioneCorseService.addDisp(new DispDTO(idFermata, new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso)));
        simpMessagingTemplate.convertAndSend("/dispws-add/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), dispTurnoResource.getDisp());
        simpMessagingTemplate.convertAndSendToUser(dispTurnoResource.getDisp().getGuideUsername(), "/dispws/" + "/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), dispTurnoResource);
        return dispTurnoResource.getDisp();
    }

    /**
     * Permette a una guide di segnalare la propria disponibilità
     */
    @PostMapping("/disp/{idDisp}")
    public DispAllResource updateDisp(@PathVariable("idDisp") String idDisp, @RequestBody DispAllResource disp) throws Exception {
        DispTurnoResource res = gestioneCorseService.updateDisp(idDisp, disp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpMessagingTemplate.convertAndSend("/dispws-up/" + dateFormat.format(res.getTurno().getData()) + "/" + res.getTurno().getIdLinea() + "/" + ((res.getTurno().getVerso()) ? 1 : 0), res.getDisp());
        simpMessagingTemplate.convertAndSendToUser(res.getDisp().getGuideUsername(), "/dispws/" + dateFormat.format(res.getTurno().getData()) + "/" + res.getTurno().getIdLinea() + "/" + ((res.getTurno().getVerso()) ? 1 : 0), res);
        return res.getDisp();
    }


    /**
     * Permette a una guide di annullare la propria disponibilità
     */
    @DeleteMapping("/disp/{idLinea}/{verso}/{data}")
    public void deleteDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        DispAllResource deleted = gestioneCorseService.deleteDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
        simpMessagingTemplate.convertAndSend("/dispws/" + deleted.getGuideUsername() + "/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), new DispTurnoResource());
        simpMessagingTemplate.convertAndSendToUser(deleted.getGuideUsername(), "/dispws/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), new DispTurnoResource());
        simpMessagingTemplate.convertAndSend("/dispws-del/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), deleted);
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
        return gestioneCorseService.getTurnoState(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
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
        TurnoEntity turno = gestioneCorseService.setTurnoState(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso), isOpen);
        TurnoResource tr = new TurnoResource(turno);
        simpMessagingTemplate.convertAndSend("/turnows/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), tr);
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
        return gestioneCorseService.getAllTurnoDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }

    /**
     * Permette di specificare quali degli accompagnatori è stato confermato
     *2909
     * @param idLinea
     * @param verso
     * @param data
     * @param dispResource
     * @throws Exception
     */
    @PostMapping("/turno/disp/{idLinea}/{verso}/{data}")
    public void setAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody DispAllResource dispResource) throws Exception {
        gestioneCorseService.setAllTurnoDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso), dispResource);
        DispStateResource state = new DispStateResource(dispResource);
        simpMessagingTemplate.convertAndSendToUser(dispResource.getGuideUsername(), "/dispws-status/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), state);
        simpMessagingTemplate.convertAndSend("/dispws-status/" + data + "/" + idLinea + "/" + ((verso) ? 1 : 0), dispResource);
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
        gestioneCorseService.ackDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }


}