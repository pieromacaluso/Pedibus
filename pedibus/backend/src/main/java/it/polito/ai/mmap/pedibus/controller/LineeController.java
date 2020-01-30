package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
import it.polito.ai.mmap.pedibus.configuration.PedibusString;
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
     * Restituisce una lista degli id delle lines presenti nel DB.
     *
     * @return Lista nomi Linee nel DB
     */
    @GetMapping("/lines")
    @ApiOperation("Restituisce gli id delle linee presenti")
    public List<String> getLines() {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/lines"));
        return lineeService.getAllLinesIds();
    }

    /**
     * Restituisce una lista dei nomi delle lines presenti nel DB.
     *
     * @return Lista nomi Linee nel DB
     */
    @GetMapping("/lines/name")
    @ApiOperation("Restituisce i nomi delle linee presenti")
    public List<String> getLinesNames() {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/lines/name"));
        return lineeService.getAllLinesNames();
    }

    /**
     * Restituisce due liste, riportanti i dettagli delle fermate di andata e ritorno.
     *
     * @param idLinea id della Linea
     * @return LineaDTO
     */
    @GetMapping("/lines/{id_linea}")
    @ApiOperation("Restituisce i dettagli delle fermate per la linea specificata")
    public LineaDTO getStopsLine(@PathVariable("id_linea") String idLinea) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/lines/"+idLinea));
        return lineeService.getLineaDTOById(idLinea);
    }


    /**
     * Dato l'id di una fermata restituisce l'oggetto fermata
     *
     * @param idFermata id della fermata
     * @return FermataDTO
     */
    @GetMapping("/lines/stops/{id_fermata}")
    @ApiOperation("Restituisce la fermata richiesta")
    public FermataDTO getStopById(@PathVariable("id_fermata") Integer idFermata) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/lines/stop/"+idFermata));
        return lineeService.getFermataDTOById(idFermata);
    }


}
