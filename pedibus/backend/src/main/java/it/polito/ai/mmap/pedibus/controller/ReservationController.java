package it.polito.ai.mmap.pedibus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.PrenotazioneEntity;
import it.polito.ai.mmap.pedibus.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.pedibus.resources.GetChildrenNotReservedLineaDataResource;
import it.polito.ai.mmap.pedibus.resources.GetReservationsNomeLineaDataResource;
import it.polito.ai.mmap.pedibus.resources.HandledResource;
import it.polito.ai.mmap.pedibus.resources.PrenotazioneResource;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /*@Value("${arrivoScuola}")
    String arrivoScuola;

    @Value("${partenzaScuola}")
    String partenzaScuola;*/

    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param nomeLinea nome linea
     * @param data      data in esame
     * @return GetReservationsNomeLineaDataResource
     */

    @GetMapping("/reservations/{nome_linea}/{data}")
    public GetReservationsNomeLineaDataResource getReservations(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) {
        logger.info("GET /reservations/" + nomeLinea + "/" + data + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        boolean canModify = reservationService.canModify(nomeLinea, dataFormatted);
        GetReservationsNomeLineaDataResource getReservationsNomeLineaDataResource = new GetReservationsNomeLineaDataResource(nomeLinea, dataFormatted, lineeService, reservationService, canModify);
       /* getReservationsNomeLineaDataResource.setArrivoScuola(arrivoScuola);
        getReservationsNomeLineaDataResource.setPartenzaScuola(partenzaScuola);*/
        return getReservationsNomeLineaDataResource;
    }

    /**
     * Restituisce un oggetto JSON contenente una liste, riportanti, per ogni fermata di andata o ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param nomeLinea nome linea
     * @param data      data in esame
     * @return GetReservationsNomeLineaDataResource
     */

    @GetMapping("/reservations/verso/{nome_linea}/{data}/{verso}")
    public GetReservationsNomeLineaDataResource getReservationsToward(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("GET /reservations/" + nomeLinea + "/" + data + "/" + verso + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        boolean canModify = reservationService.canModify(nomeLinea, dataFormatted);
        GetReservationsNomeLineaDataResource getReservationsNomeLineaDataResource =  new GetReservationsNomeLineaDataResource(nomeLinea, dataFormatted, lineeService, userService, reservationService, verso, canModify);
        /*getReservationsNomeLineaDataResource.setArrivoScuola(arrivoScuola);
        getReservationsNomeLineaDataResource.setPartenzaScuola(partenzaScuola);*/
        return getReservationsNomeLineaDataResource;
    }

    /**
     * Restituisce la lista dei bambini non prenotati per la data(AAAA-MM-GG) e il verso passati.
     *
     * @param data
     * @param verso
     * @return
     */

    @GetMapping("/notreservations/{data}/{verso}")
    public GetChildrenNotReservedLineaDataResource getNotReservations(@PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("GET /NotReservations/" + data + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        return new GetChildrenNotReservedLineaDataResource(dataFormatted, verso, reservationService, userService);
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
    public String postReservation(@RequestBody PrenotazioneResource prenotazioneResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        logger.info("Nuova Prenotazione" + prenotazioneResource.toString());
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, lineeService.getLineById(nomeLinea).getId(), dataFormatted);
        String idPrenotazione = reservationService.addPrenotazione(prenotazioneDTO);
        simpMessagingTemplate.convertAndSend("/reservation/" + data + "/" + nomeLinea + "/" + ((prenotazioneResource.getVerso()) ? 1 : 0), prenotazioneResource);
        return mapper.writeValueAsString(idPrenotazione);
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
        PrenotazioneDTO prenotazioneDTO = new PrenotazioneDTO(prenotazioneResource, lineeService.getLineById(nomeLinea).getId(), dataFormatted);
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

        if (lineeService.getLineById(nomeLinea).getId().equals(checkPren.getIdLinea()) && dataFormatted.equals(checkPren.getData()))
            return new PrenotazioneDTO(checkPren);
        else
            throw new IllegalArgumentException("Prenotazione non esistente");
    }

    /**
     * Usato da admin linea per indicare che ha preso il bambino dalla fermata
     *
     * @param nomeLinea
     * @param verso
     * @param data
     * @param cfChild   true per indicare che è stato preso, false per annullare
     */
    @PostMapping("/reservations/handled/{nomeLinea}/{verso}/{data}/{isSet}")
    public void manageHandled(@PathVariable("nomeLinea") String nomeLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @PathVariable("isSet") Boolean isSet, @RequestBody String cfChild, HttpServletResponse response) throws Exception {
        Date date = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);

        Integer idFermata = reservationService.manageHandled(verso, date, cfChild, isSet);
        if (idFermata != -1) {
            simpMessagingTemplate.convertAndSend("/handled/" + data + "/" + nomeLinea + "/" + ((verso) ? 1 : 0), new HandledResource(cfChild, isSet, idFermata));
            logger.info("/handled/" + data + "/" + nomeLinea + "/" + verso);
        }

    }


    /**
     * Usato da admin linea per indicare che ha lasciato il bambino a scuola
     *
     * @param verso
     * @param data
     * @param cfChild
     * @param isSet   true per indicare che è arrivato, false per annullare
     */

    @PostMapping("/reservations/arrived/{verso}/{data}/{isSet}")
    public void manageArrived(@PathVariable("verso") Boolean verso, @PathVariable("data") String data, @PathVariable("isSet") Boolean isSet, @RequestBody String cfChild) throws Exception {
        Date date = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        if (reservationService.manageArrived(verso, date, cfChild, isSet))
            logger.info("Child " + cfChild + " is arrived");
    }

}
