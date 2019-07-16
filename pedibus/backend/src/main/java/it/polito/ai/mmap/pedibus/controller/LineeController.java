package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.configuration.DbTestDataCreator;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.services.JsonHandlerService;
import it.polito.ai.mmap.pedibus.services.LineeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@RestController
public class LineeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JsonHandlerService jsonHandlerService;

    @Autowired
    private Environment environment;

    @Autowired
    LineeService lineeService;

    @Autowired
    DbTestDataCreator dbTestDataCreator;

    /**
     * Metodo eseguito all'avvio della classe come init per leggere le linee del pedibus.
     */
    @PostConstruct
    public void init() throws IOException {
        logger.info("Caricamento linee in corso...");
        jsonHandlerService.readPiedibusLines();
        logger.info("Caricamento linee completato.");
        if (environment.getActiveProfiles()[0].equals("prod")) {
            logger.info("Creazione Basi di dati di test in corso...");
            dbTestDataCreator.makeChildUserReservations();
            logger.info("Creazione Basi di dati di test completata.");
        } else {
            logger.info("Creazione Basi di dati di test non effettuata con DEV Profile");
        }
    }

    /**
     * Mapping verso la home dell'applicazione.
     *
     * @return home
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Restituisce una JSON con una lista dei nomi delle lines presenti nel DB.
     *
     * @return Lista nomi Linee nel DB
     */
    @GetMapping("/lines")
    public List<String> getLines() {
        return lineeService.getAllLinesIds();
    }
    /**
     * Restituisce una JSON con una lista dei nomi delle lines presenti nel DB.
     *
     * @return Lista nomi Linee nel DB
     */
    @GetMapping("/lines/name")
    public List<String> getLinesNames() {
        return lineeService.getAllLinesNames();
    }

    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti i dettagli delle fermate di andata e ritorno.
     *
     * @param name nome Linea
     * @return LineaDTO
     */
    @GetMapping("/lines/{nome_linea}")
    public LineaDTO getStopsLine(@PathVariable("nome_linea") String name) {
        return lineeService.getLineById(name);
    }


}
