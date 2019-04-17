package it.polito.ai.mmap.esercitazione2.controller;
//TODO fermare server o comunque gestire il fatto che non ci sia connessione a internet


import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione2.resources.GetReservationsNomeLineaDataResource;
import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import it.polito.ai.mmap.esercitazione2.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

@RestController
public class HomeController {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JsonHandlerService jsonHandlerService;

    @Autowired
    MongoService mongoService;

    /**
     * Metodo eseguito all'avvio della classe come init per leggere le linee del pedibus
     */
    @PostConstruct
    public void init() {
        logger.info("Caricamento linee in corso...");
        jsonHandlerService.readPiedibusLines();
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
     * Restituisce una JSON con una lista dei nomi delle lines presenti nel DBMS
     */
    @GetMapping("/lines")
    public List<String> getLines() {
        return mongoService.getAllLinesNames();
    }

    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti i dettagli delle fermate di andata e ritorno
     */
    @GetMapping("/lines/{nome_linea}")
    public LineaDTO getStopsLine(@PathVariable("nome_linea") String name) {
        return mongoService.getLineByName(name);
    }

    /**
     * Restituisce un oggetto JSON contenente due liste,riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata
     */
    @GetMapping("/reservations/{nome_linea}/{data}")
    public GetReservationsNomeLineaDataResource getReservations(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        Date dataFormatted = getDate(data);
        return new GetReservationsNomeLineaDataResource(nomeLinea, dataFormatted, mongoService);
    }

    /**
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui sale/scende e il verso di percorrenza (andata/ritorno);
     * restituisce un identificatore univoco della prenotazione creata
     */
    @PostMapping("/reservations/{nome_linea}/{data}")
    public String postReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        Date dataFormatted = getDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, mongoService.getLineByName(nomeLinea).getId(), dataFormatted);
        return mongoService.addPrenotazione(prenotazioneDTO);
    }


    /**
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla prenotazione indicata
     * il reservation_id ci permette di identificare la prenotazione da modificare, il body contiene i dati aggiornati
     */
    @PutMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void updateReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date dataFormatted = getDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, mongoService.getLineByName(nomeLinea).getId(), dataFormatted);
        mongoService.updatePrenotazione(prenotazioneDTO, reservationId);
    }

    /**
     * Elimina la prenotazione indicata
     */
    @DeleteMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void deleteReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date dataFormatted = getDate(data);
        mongoService.deletePrenotazione(nomeLinea, dataFormatted, reservationId);
    }

    /**
     * Restituisce la prenotazione controllando che nomeLinea e Data corrispondano a quelli del reservation_id
     *
     * @param nomeLinea
     * @param data
     * @param reservationId
     * @return
     */
    @GetMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public PrenotazioneDTO getReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date dataFormatted = getDate(data);
        PrenotazioneEntity checkPren = mongoService.getPrenotazione(reservationId);

        if (mongoService.getLineByName(nomeLinea).getId().equals(checkPren.getIdLinea()) && dataFormatted.equals(checkPren.getData()))
            return new PrenotazioneDTO(checkPren);
        else
            throw new IllegalArgumentException("Linea non esistente");
    }

    public Date getDate(String data) {
        String completeData = data + " 12:00 GMT+00:00"; //data nel formato AAAA-MM-DD
        String pattern = "yyyy-MM-dd HH:mm z";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, dateTimeFormatter);
        return Date.from(londonTime.toInstant());
    }
}
