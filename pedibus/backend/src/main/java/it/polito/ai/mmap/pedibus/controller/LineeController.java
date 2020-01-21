package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LineeController {
    @Autowired
    LineeService lineeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Mapping verso la home dell'applicazione.
     *
     * @return home
     * @deprecated Non più utilizzato
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
     * @param idLinea id della Linea
     * @return LineaDTO
     */
    @GetMapping("/lines/{id_linea}")
    public LineaDTO getStopsLine(@PathVariable("id_linea") String idLinea) {
        return lineeService.getLineaDTOById(idLinea);
    }


    /**
     * Dato l'id di una fermata restituisce un oggetto JSON contenente la fermata
     *
     * @param idFermata id della fermata
     * @return FermataDTO
     */
    @GetMapping("/lines/stops/{id_fermata}")
    public FermataDTO getStopById(@PathVariable("id_fermata") Integer idFermata) {
        return lineeService.getFermataDTOById(idFermata);
    }


}
