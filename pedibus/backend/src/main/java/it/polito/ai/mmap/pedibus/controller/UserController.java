package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.resources.ChildDefaultStopResource;
import it.polito.ai.mmap.pedibus.services.ChildService;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    ChildService childService;

    @Autowired
    ReservationService reseService;

    @Autowired
    MongoTimeService mongoTimeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Tramite questo endpoint un utente loggato recupera la lista dei suoi figli
     *
     * @return
     */
    @GetMapping("/children")
    public List<ChildDTO> getMyChildren() {
        return childService.getMyChildren();
    }

    /**
     * Restituisce lo status di un bambino dato data e verso, lo status indica se il bambino è stato preso in carico o è arrivato a scuola
     * @param cfChild codice fiscale
     * @param date data
     */
    @GetMapping("/children/stops/{cfChild}/{date}")
    public List<ReservationDTO> getChildStatus(@PathVariable("cfChild") String cfChild, @PathVariable("date") String date) {
        logger.info("GET /children/stops/" + cfChild + "/" + date  + " è stato contattato");
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(date);
        return this.reseService.getChildReservationsAR(dataFormatted, cfChild);

    }


    /**
     * Permette ad un genitore di cambiare le fermate di default di uno dei figli
     *
     * @param cfChild
     * @param stopRes
     */
    @PutMapping("/children/stops/{cfChild}")
    public void updateChildStop(@PathVariable("cfChild") String cfChild, @RequestBody ChildDefaultStopResource stopRes) {
        logger.info("PUT /children/stops/" + cfChild + " è stato contattato");

        childService.updateChildStop(cfChild, stopRes);
    }


}
