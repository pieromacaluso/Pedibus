package it.polito.ai.mmap.esercitazione2.controller;

import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        return mongoService.getLine(name);
    }

    /**
     * Restituisce un oggetto JSON contenente due liste,riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata
     */
    @GetMapping("/reservations/{nome_linea}/{data}")
    public GetReservationsNomeLineaDataResource getReservations(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        Date date=getDate(data);
        return new GetReservationsNomeLineaDataResource(nomeLinea,date,mongoService);
    }

    /**
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui sale/scende e il verso di percorrenza (andata/ritorno);
     * restituisce un identificatore univoco della prenotazione creata
     */
    @PostMapping("/reservations/{nome_linea}/{data}")
    public String postReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        Date date=getDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, mongoService.getLine(nomeLinea).getId(), date);
        String id=mongoService.addPrenotazione(prenotazioneDTO);
        return id;      //todo verificare se va bene,in caso di errore ritorna "prenotazione non valida"
    }


    /**
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla prenotazione indicata
     * il reservation_id ci permette di identificare la prenotazione da modificare, il body contiene i dati aggiornati
     */
    @PutMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void updateReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data")  String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date date=getDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, mongoService.getLine(nomeLinea).getId(), date);
        mongoService.updatePrenotazione(prenotazioneDTO,reservationId);
    }

    /**
     * Elimina la prenotazione indicata
     *
     */
    @DeleteMapping("/reservations/{nome_linea}/{data}/{reservation_id}")        //todo check forse non cancella dal db
    public void deleteReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date date=getDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, mongoService.getLine(nomeLinea).getId(), date);
        mongoService.deletePrenotazione(prenotazioneDTO,reservationId);
    }

    /**
     * Restituisce la prenotazione
     * @param nomeLinea
     * @param data
     * @param reservationId
     * @return
     */
    @GetMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public PrenotazioneDTO getReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        PrenotazioneEntity prenotazioneEntity=mongoService.getPrenotazione(reservationId);
        LineaEntity lineaEntity=new LineaEntity(mongoService.getLine(nomeLinea));
        if(lineaEntity.getId()==prenotazioneEntity.getIdLinea()){
            return new PrenotazioneDTO(prenotazioneEntity,lineaEntity.getId());
        }else{
            return null;    //todo parametro da restituire per errore
        }
    }

    public Date getDate(String data){      //data nel formato AAAA-MM-DD
        StringTokenizer t = new StringTokenizer(data,"-");
        int AAAA=Integer.valueOf(t.nextToken());
        int MM=Integer.valueOf(t.nextToken());
        int DD=Integer.valueOf(t.nextToken());
        ZoneOffset zoneOffset=ZoneOffset.of("+00:00");
        ZonedDateTime londonTime=ZonedDateTime.of(AAAA,MM,DD,12,00,00,00, zoneOffset);
        return Date.from(londonTime.toInstant());
    }
}
