package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.services.DataCreationService;
import it.polito.ai.mmap.pedibus.repository.*;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ChildRepository childRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    DataCreationService dataCreationService;

    @PostMapping("/debug/delete")
    public void deletAll()
    {
        userRepository.deleteAll();
        childRepository.deleteAll();
        reservationRepository.deleteAll();
        logger.info("Child, user e reservations sono state cancellate");

    }

    @PostMapping("/debug/make")
    public void makeChildUserReservations() throws IOException {
        logger.info("POST /debug/make è stato contattato");
        dataCreationService.makeChildUserReservations();
    }

    @PostMapping("/debug/make/notifications")
    public void makeNotifications() throws IOException {
        logger.info("POST /debug/make/notifications è stato contattato");
        dataCreationService.makeNotifications();
    }





}
