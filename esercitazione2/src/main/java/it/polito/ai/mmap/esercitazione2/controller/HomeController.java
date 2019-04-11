package it.polito.ai.mmap.esercitazione2.controller;

import it.polito.ai.mmap.esercitazione2.resources.LineaResource;
import it.polito.ai.mmap.esercitazione2.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import javax.annotation.PostConstruct;

@RestController
public class HomeController {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JsonHandlerService jsonHandlerService;
    //MongoService mongoService;

    /*Metodo eseguito all'avvio della classe come init per leggere le linee del pedibus
     * */
    @PostConstruct
    public void init() throws Exception {
        logger.info("Caricamento linee in corso...");
        jsonHandlerService.readPiedibusLines();             //todo verificare se possibile evitare se non ci sono modifiche
        logger.info("Caricamento linee completato.");
    }

    /**
     * Mapping verso la home dell'applicazione
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Restituisce una lista JSON con solo i nomi delle lines presenti nel DBMS
     */
    @GetMapping("/lines")
    public String getLines() {
        return jsonHandlerService.getAllNameLines();
    }

    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti i dettagli delle fermate di andata e ritorno
     */
    @GetMapping("/lines/{nome_linea}")
    public LineaResource getStopsLine(@PathVariable("nome_linea") String name) {
        LineaResource lineaResource = new LineaResource(jsonHandlerService.getLine(name));
        //lineaResource.add(ControllerLinkBuilder.linkTo(HomeController.class).slash(jsonHandlerService).withSelfRel());

        return lineaResource;
    }

    /**
     * Restituisce un oggetto JSON contenente due liste,riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     *  persone che devono essere prese in carico o lasciate in corrispondenza della fermata
     */
    @GetMapping("/reservation/{nome_linea}/{data}")
    public String getReservations() {

        return "reservationsJSON";
    }

    /*
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui
     *  sale/scende e il verso di percorrenza (andata/ritorno); restituisce un identificatore univoco della prenotazione
     *  creata
     */
    @PostMapping("/reservation/{nome_linea}/{data}")
    public String postReservation() {
        //TODO usare @RequestBody per convertire automaticamente il json in ingresso in un oggetto prenotazione
        return "reservationJSON";
    }

    /*
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla prenotazione indicata
     *
     */
    @PutMapping("/reservation/{nome_linea}/{data}/{reservation_id}")
    public String updateReservation() {

        return "updatedJSON";
    }

    /*
     * Elimina la prenotazione indicata
     *
     */
    @DeleteMapping("/reservation/{nome_linea}/{data}/{reservation_id}")
    public String deleteReservation() {

        return "deleted";
    }

    /*
     * Restituisce la prenotazione
     */
    @GetMapping("/reservation/{nome_linea}/{data}/{reservation_id}")
    public String getReservation() {

        return "reservation";
    }


}
