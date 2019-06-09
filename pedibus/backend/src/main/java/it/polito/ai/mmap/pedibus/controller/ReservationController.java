package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.PrenotazioneEntity;
import it.polito.ai.mmap.pedibus.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.pedibus.resources.GetChildrenNotReservedLineaDataResource;
import it.polito.ai.mmap.pedibus.resources.GetReservationsNomeLineaDataResource;
import it.polito.ai.mmap.pedibus.resources.PrenotazioneResource;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
public class ReservationController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LineeService lineeService;
    @Autowired
    ReservationService reservationService;
    @Autowired
    UserService userService;


    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param nomeLinea nome linea
     * @param data      data in esame
     * @return GetReservationsNomeLineaDataResource
     */
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/reservations/{nome_linea}/{data}")
    public GetReservationsNomeLineaDataResource getReservations(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        logger.info("GET /reservations/" + nomeLinea + "/" + data + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        return new GetReservationsNomeLineaDataResource(nomeLinea, dataFormatted, lineeService, reservationService);
    }

    @GetMapping("/notreservations/{data}/{verso}")
    public GetChildrenNotReservedLineaDataResource getNotReservations(@PathVariable("data") String data,@PathVariable("verso")boolean verso) {
        logger.info("GET /NotReservations/" + data + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        return new GetChildrenNotReservedLineaDataResource(dataFormatted, verso, reservationService,userService);
    }

    /**
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui sale/scende e il verso di percorrenza (andata/ritorno);
     * restituisce un identificatore univoco della prenotazione creata
     *
     * @param prenotazioneResource JSON Body Prenotazione
     * @param nomeLinea            nome linea
     * @param data                 data in esame
     * @return identificatore univoco prenotazione
     */
    @PostMapping("/reservations/{nome_linea}/{data}")
    public String postReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, lineeService.getLineByName(nomeLinea).getNome(), dataFormatted);
        return reservationService.addPrenotazione(prenotazioneDTO);
    }


    /**
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla prenotazione indicata.
     * Il reservation_id ci permette di identificare la prenotazione da modificare, il body contiene i dati aggiornati.
     *
     * @param prenotazioneResource JSON Body Prenotazione
     * @param nomeLinea            nome linea
     * @param data                 data in esame
     * @param reservationId        id Prenotazione da aggiornare
     */
    @PutMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void updateReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, lineeService.getLineByName(nomeLinea).getNome(), dataFormatted);
        reservationService.updatePrenotazione(prenotazioneDTO, reservationId);
    }

    /**
     * Elimina la prenotazione indicata
     *
     * @param nomeLinea     nome linea
     * @param data          data in esame
     * @param reservationId id prenotazione
     */
    @DeleteMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void deleteReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        reservationService.deletePrenotazione(nomeLinea, dataFormatted, reservationId);
    }

    /**
     * Restituisce la prenotazione controllando che nomeLinea e Data corrispondano a quelli del reservation_id
     *
     * @param nomeLinea     nome linea
     * @param data          data in esame
     * @param reservationId id prenotazione
     * @return PrenotazioneDTO
     */
    @GetMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public PrenotazioneDTO getReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        //todo spostare nel service per non appesantire controller
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        PrenotazioneEntity checkPren = reservationService.getReservationFromId(reservationId);

        if (lineeService.getLineByName(nomeLinea).getNome().equals(checkPren.getNomeLinea()) && dataFormatted.equals(checkPren.getData()))
            return new PrenotazioneDTO(checkPren);
        else
            throw new IllegalArgumentException("Prenotazione non esistente");
    }

    /**
     * Usato da admin linea per indicare che ha preso il bambino dalla fermata
     *
     * @param verso
     * @param data
     * @param child
     */
    @PostMapping("/reservation/handled/{verso}/{data}")
    public void SetHandled(@PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody String child, HttpServletResponse response) throws Exception {
        reservationService.setHandled(verso, data, child);
    }


    /**
     * Usato da admin linea per indicare che ha lasciato il bambino a scuola
     *
     * @param verso
     * @param data
     * @param child
     */
    @PostMapping("/reservation/arrived/{verso}/{data}")
    public void SetArrived(@PathVariable("verso") Boolean verso, @PathVariable("data") String data, @RequestBody String child) throws Exception {
        reservationService.setArrived(verso, data, child);
    }

}
