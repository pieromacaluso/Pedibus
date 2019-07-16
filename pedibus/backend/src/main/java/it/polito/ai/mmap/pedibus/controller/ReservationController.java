package it.polito.ai.mmap.pedibus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.resources.*;
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
    ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


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
        return new GetReservationsNomeLineaDataResource(nomeLinea, dataFormatted, lineeService, reservationService, canModify);
    }

    /**
     * Restituisce un oggetto JSON contenente una lista, riportante, per ogni fermata di andata o ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param nomeLinea nome linea
     * @param data      data in esame
     * @return GetReservationsNomeLineaDataVersoResource
     */

    @GetMapping("/reservations/verso/{nome_linea}/{data}/{verso}")
    public GetReservationsNomeLineaDataVersoResource getReservationsToward(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("GET /reservations/" + nomeLinea + "/" + data + "/" + verso + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        boolean canModify = reservationService.canModify(nomeLinea, dataFormatted);
        return new GetReservationsNomeLineaDataVersoResource(nomeLinea, dataFormatted, lineeService, userService, reservationService, verso, canModify);
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
        logger.info("GET /notreservations/" + data + " è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        return new GetChildrenNotReservedLineaDataResource(dataFormatted, verso, reservationService, userService);
    }

    /**
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui sale/scende e il verso di percorrenza (andata/ritorno);
     * restituisce un identificatore univoco della reservation creata
     *
     * @param reservationResource JSON Body Reservation
     * @param nomeLinea            nome linea
     * @param data                 data in esame
     * @return identificatore univoco reservation
     */
    @PostMapping("/reservations/{nome_linea}/{data}")
    public String postReservation(@RequestBody ReservationResource reservationResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data) throws JsonProcessingException {
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        logger.info("Nuova Reservation " + reservationResource.toString());
        ReservationDTO reservationDTO = new ReservationDTO(reservationResource, lineeService.getLineById(nomeLinea).getId(), dataFormatted);
        String idReservation = reservationService.addReservation(reservationDTO);
        simpMessagingTemplate.convertAndSend("/reservation/" + data + "/" + nomeLinea + "/" + ((reservationResource.getVerso()) ? 1 : 0), reservationResource);
        return objectMapper.writeValueAsString(idReservation);
    }


    /**
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla reservation indicata.
     * Il reservation_id ci permette di identificare la reservation da modificare, il body contiene i dati aggiornati.
     *
     * @param reservationResource JSON Body Reservation
     * @param nomeLinea            nome linea
     * @param data                 data in esame
     * @param reservationId        id Reservation da aggiornare
     */
    @PutMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void updateReservation(@RequestBody ReservationResource reservationResource, @PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("Aggiornamento reservation " + reservationId);
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        ReservationDTO reservationDTO = new ReservationDTO(reservationResource, lineeService.getLineById(nomeLinea).getId(), dataFormatted);
        reservationService.updateReservation(reservationDTO, reservationId);
    }

    /**
     * Elimina la reservation indicata
     *
     * @param nomeLinea     nome linea
     * @param data          data in esame
     * @param reservationId id reservation
     */
    @DeleteMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public void deleteReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("Eliminazione reservation" + reservationId);
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        reservationService.deleteReservation(nomeLinea, dataFormatted, reservationId);
    }

    /**
     * Restituisce la reservation controllando che nomeLinea e Data corrispondano a quelli del reservation_id
     *
     * @param nomeLinea     nome linea
     * @param data          data in esame
     * @param reservationId id reservation
     * @return ReservationDTO
     */
    @GetMapping("/reservations/{nome_linea}/{data}/{reservation_id}")
    public ReservationDTO getReservation(@PathVariable("nome_linea") String nomeLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("/reservations/{nome_linea}/{data}/{reservation_id} è stato contattato");
        Date dataFormatted = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        return reservationService.getReservationCheck(nomeLinea, dataFormatted, reservationId);
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

        Integer idFermata = reservationService.manageHandled(verso, date, cfChild, isSet, nomeLinea);
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

    @PostMapping("/reservations/arrived/{nomeLinea}/{verso}/{data}/{isSet}")
    public void manageArrived(@PathVariable("verso") String nomeLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @PathVariable("isSet") Boolean isSet, @RequestBody String cfChild) throws Exception {
        Date date = MongoZonedDateTime.getMongoZonedDateTimeFromDate(data);
        if (reservationService.manageArrived(verso, date, cfChild, isSet, nomeLinea))
            logger.info("Child " + cfChild + " is arrived");
    }

}
