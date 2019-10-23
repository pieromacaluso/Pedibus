package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.resources.DispTurnoResource;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Null;
import java.util.List;

//TODO implementare la sicurezza degli endpoint

@RestController
public class GestioneCorseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    GestioneCorseService gestioneCorseService;

    /**
     * Permette a una guide/admin di una linea di segnalare la propria disponibilità
     */
    @GetMapping("/disp/{idLinea}/{verso}/{data}")
    public DispTurnoResource getDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        return gestioneCorseService.getDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
    }

    /**
     * Permette a una guide/admin di una linea di segnalare la propria disponibilità
     */
    @PostMapping("/disp/{idLinea}/{verso}/{data}")
    public void addDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Integer idFermata) throws Exception {
        gestioneCorseService.addDisp(new DispDTO(idFermata, new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso)));
    }


    /**
     * Permette a una guide/admin di una linea di annullare la propria disponibilità
     */
    @DeleteMapping("/disp/{idLinea}/{verso}/{data}")
    public void deleteDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
        gestioneCorseService.deleteDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
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
    public List<DispAllResource> getAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) throws Exception {
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
        gestioneCorseService.ackDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso));
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
        gestioneCorseService.setTurnoState(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso), isOpen);
    }


}