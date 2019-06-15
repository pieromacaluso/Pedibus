package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.configuration.DbTestDataCreator;
import it.polito.ai.mmap.pedibus.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * - una post a http://localhost:8080/debug/make genera:
 * <p>
 * - 100 Child
 * - 50 genitori con 2 figli        username = primi 50 contenuti nel file genitori.json e pw = 1!qwerty1!
 * - 50 nonni                       username = secondi 50 contenuti nel file genitori.json e pw = 1!qwerty1!
 * - 1 prenotazione/figlio per oggi, domani e dopo domani (o andata o ritorno)
 */
@Profile("dev")
@RestController
public class DebugController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ChildRepository childRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PrenotazioneRepository prenotazioneRepository;

    @Autowired
    DbTestDataCreator dbTestDataCreator;

    @PostMapping("/debug/delete")
    public void deletAll()
    {
        userRepository.deleteAll();
        childRepository.deleteAll();
        prenotazioneRepository.deleteAll();
        logger.info("Child, user e prenotazioni sono state cancellate");

    }

    @PostMapping("/debug/make")
    public void makeChildUserPrenotazioni() throws IOException {
        logger.info("POST /debug/make è stato contattato");
        dbTestDataCreator.makeChildUserPrenotazioni();
    }



}
