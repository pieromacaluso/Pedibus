package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.objectDTO.DispDTO;
import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.resources.DispTurnoResource;
import it.polito.ai.mmap.pedibus.resources.TurnoDispResource;
import it.polito.ai.mmap.pedibus.resources.TurnoResource;
import it.polito.ai.mmap.pedibus.services.GestioneCorseService;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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
     *
     * @param verso verso
     * @param data  data
     * @return dati su disponibilità e turno
     */
    @ApiOperation("Restituisce la disponibilità del richiedente e lo stato del relativo turno")
    @GetMapping("/disp/{verso}/{data}")
    public DispTurnoResource getDisp(@PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/disp/" + verso + "/" + data));
        return gestioneCorseService.getDispTurnoResource(mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso);
    }

    /**
     * Permette a una guide di segnalare la propria disponibilità
     *
     * @param idLinea   id linea
     * @param verso     verso linea
     * @param data      data
     * @param idFermata id fermata
     * @return disponibilità segnalata
     */
    @PostMapping("/disp/{idLinea}/{verso}/{data}")
    @ApiOperation("Permette di segnalare la propria disponibilità")
    public DispAllResource addDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Integer idFermata) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/disp/" + idLinea + "/" + verso + "/" + data));
        DispTurnoResource dispTurnoResource = gestioneCorseService.addDisp(new DispDTO(idFermata, new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso)));
        return dispTurnoResource.getDisp();
    }

    /**
     * Permette a una guide di annullare la propria disponibilità
     *
     * @param idLinea id linea
     * @param verso   verso linea
     * @param data    data linea
     */
    @DeleteMapping("/disp/{idLinea}/{verso}/{data}")
    @ApiOperation("Permette di annullare una disponibilità")
    public void deleteDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        logger.info(PedibusString.ENDPOINT_CALLED("DELETE", "/disp/" + idLinea + "/" + verso + "/" + data));
        gestioneCorseService.deleteDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }

    /**
     * Permette all'admin di una linea di recuperare lo stato di un turno
     *
     * @param idLinea id linea
     * @param verso   verso linea
     * @param data    data
     */
    @GetMapping("/turno/state/{idLinea}/{verso}/{data}")
    @ApiOperation("Restituisce lo stato di un turno")
    public TurnoResource getTurnoState(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/turno/state/" + idLinea + "/" + verso + "/" + data));
        return gestioneCorseService.getTurnoState(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }

    /**
     * Permette all'admin di una linea di gestire lo stato di un turno
     *
     * @param idLinea id linea
     * @param verso   verso linea
     * @param data    data turno
     * @param isOpen  boolean se è aperto o meno
     */
    @PutMapping("/turno/state/{idLinea}/{verso}/{data}")
    @ApiOperation("Permette di chiudere/aprire un turno")
    public void setTurnoState(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody Boolean isOpen) {
        logger.info(PedibusString.ENDPOINT_CALLED("PUT", "/turno/state/" + idLinea + "/" + verso + "/" + data));
        gestioneCorseService.setTurnoState(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso), isOpen);
    }

    /**
     * Restituisce le disponibilità per il turno indicato
     *
     * @param idLinea id linea
     * @param verso   verso linea
     * @param data    data disponibilità
     * @return TurnoDispResource
     */
    @GetMapping("/turno/disp/{idLinea}/{verso}/{data}")
    @ApiOperation("Restituisce le disponibilità per il turno indicato")
    public TurnoDispResource getAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/turno/disp/" + idLinea + "/" + verso + "/" + data));
        return gestioneCorseService.getAllTurnoDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }

    /**
     * Permette di specificare quali degli accompagnatori è stato confermato
     *
     * @param idLinea      id linea
     * @param verso        verso disp
     * @param data         data
     * @param dispResource dispResource
     */
    @PostMapping("/turno/disp/{idLinea}/{verso}/{data}")
    @ApiOperation("Permette di specificare quali degli accompagnatori è stato confermato")
    public void setAllTurnoDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody DispAllResource dispResource) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/turno/disp/" + idLinea + "/" + verso + "/" + data));
        gestioneCorseService.setAllTurnoDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso), dispResource);
    }

    /**
     * Permette alle guide confermate di ack la loro conferma
     *
     * @param idLinea id linea
     * @param verso   verso linea
     * @param data    data turno
     */
    @PostMapping("/turno/disp/ack/{idLinea}/{verso}/{data}")
    @ApiOperation("Permette alle guide confermate di ack la loro conferma")
    public void ackDisp(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/turno/disp/ack/" + idLinea + "/" + verso + "/" + data));
        gestioneCorseService.ackDisp(new TurnoDTO(idLinea, mongoTimeService.getMongoZonedDateTimeFromDate(data, true), verso));
    }
}
