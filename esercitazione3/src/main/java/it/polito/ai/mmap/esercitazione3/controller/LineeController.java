package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione3.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione3.services.LineeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
public class LineeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JsonHandlerService jsonHandlerService;

    @Autowired
    LineeService lineeService;


    /**
     * Metodo eseguito all'avvio della classe come init per leggere le linee del pedibus.
     */
    @PostConstruct
    public void init() {
        logger.info("Caricamento linee in corso...");
        jsonHandlerService.readPiedibusLines();
        logger.info("Caricamento linee completato.");
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
        return lineeService.getLineByName(name);
    }


}
