package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.repository.ReservationRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.services.DataCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Profile("dev")
@RestController
public class DebugController {

    @Autowired
    ChildRepository childRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    DataCreationService dataCreationService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Endpoint di debug, cancella tutto dal database
     */
    @PostMapping("/debug/delete")
    public void deleteAll() {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/debug/delete"));
        userRepository.deleteAll();
        childRepository.deleteAll();
        reservationRepository.deleteAll();
        logger.info(PedibusString.ALL_DELETED);
    }


    /**
     * Endpoint di debug, genera dati fittizi
     *
     * @throws IOException Eccezione lettura da file
     */
    @PostMapping("/debug/make")
    public void makeChildUserReservations() throws IOException {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/debug/make"));
        dataCreationService.makeChildUserReservations();
    }

    /**
     * Endpoint di debug, genera notifiche fittizie
     *
     * @throws IOException Eccezione File
     */
    @PostMapping("/debug/make/notifications")
    public void makeNotifications() throws IOException {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/debug/make/notifications"));
        dataCreationService.makeNotifications();
    }
}
