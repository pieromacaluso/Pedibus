package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/disp")
    public void addDisp(@RequestBody DispDTO dispDTO) throws Exception {
        gestioneCorseService.addDisp(dispDTO);
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
     * @param idLinea
     * @param verso
     * @param data
     * @param usernameList
     * @throws Exception
     */
    @PostMapping("/turno/disp/{idLinea}/{verso}/{data}")
    public void setAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody List<String> usernameList) throws Exception {
        gestioneCorseService.setAllTurnoDisp(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso), usernameList);

    }

    /**
     * Permette all'admin di una linea di gestire lo stato di un turno
     * @param idLinea
     * @param verso
     * @param data
     * @param isOpen
     */
    @PostMapping("/turno/state/{idLinea}/{verso}/{data}")
    public void setTurnoState(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Boolean isOpen) {
        gestioneCorseService.setTurnoState(new TurnoDTO(idLinea, MongoZonedDateTime.getMongoZonedDateTimeFromDate(data), verso), isOpen);
    }



}