package it.polito.ai.mmap.pedibus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DebugController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/debug")
    public void setUp() {
//        childMap.values().forEach(childEntity -> {
//            PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity();
//            prenotazioneEntity.setCfChild(childEntity.getCodiceFiscale());
//            prenotazioneEntity.setData(new Date());
//            prenotazioneEntity.setIdFermata(1);
//            prenotazioneEntity.setNomeLinea("linea1");
//            prenotazioneEntity.setVerso(true);
//            prenotazioneRepository.save(prenotazioneEntity);
//        });
    }

    private void userEntityCreator() {


    }

}
