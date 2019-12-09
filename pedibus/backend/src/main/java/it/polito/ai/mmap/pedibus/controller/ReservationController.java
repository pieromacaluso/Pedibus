package it.polito.ai.mmap.pedibus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.resources.GetReservationsIdDataVersoResource;
import it.polito.ai.mmap.pedibus.resources.GetReservationsIdLineaDataResource;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RestController
public class ReservationController {
    @Autowired
    LineeService lineeService;
    @Autowired
    ReservationService reservationService;
    @Autowired
    UserService userService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MongoTimeService mongoTimeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    /**
     * Restituisce un oggetto JSON contenente due liste, riportanti, per ogni fermata di andata e ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param idLinea id della linea
     * @param data    data in esame
     * @return GetReservationsIdLineaDataResource
     */

    @GetMapping("/reservations/{id_linea}/{data}")
    public GetReservationsIdLineaDataResource getReservations(@PathVariable("id_linea") String idLinea, @PathVariable("data") String data) {
        logger.info("GET /reservations/" + idLinea + "/" + data + " è stato contattato");
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        return reservationService.getReservationsResource(idLinea, dataFormatted);
    }

    /**
     * Restituisce un oggetto JSON contenente una lista, riportante, per ogni fermata di andata o ritorno, l’elenco delle
     * persone che devono essere prese in carico o lasciate in corrispondenza della fermata.
     *
     * @param idLinea id della linea
     * @param data    data in esame
     * @return GetReservationsidLineaDataVersoResource
     */

    @GetMapping("/reservations/verso/{id_linea}/{data}/{verso}")
    public GetReservationsIdDataVersoResource getReservationsToward(@PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("GET /reservations/verso/" + idLinea + "/" + data + "/" + verso + " è stato contattato");
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        return reservationService.getReservationsVersoResource(idLinea, dataFormatted, verso);
    }

    /**
     * Restituisce la lista dei bambini non prenotati per la data(AAAA-MM-GG) e il verso passati.
     *
     * @param data
     * @param verso
     * @return
     */

    @GetMapping("/notreservations/{data}/{verso}")
    public List<ChildDTO> getNotReservations(@PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("GET /notreservations/" + data + " è stato contattato");
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        return reservationService.getChildrenNotReserved(dataFormatted, verso);
    }


    /**
     * Restituisce la reservation controllando che idLinea e Data corrispondano a quelli del reservation_id
     *
     * @param idLinea       id linea
     * @param data          data in esame
     * @param reservationId id reservation
     * @return ReservationDTO
     */
    @GetMapping("/reservations/{id_linea}/{data}/{reservation_id}")
    public ReservationDTO getReservation(@PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("/reservations/{id_linea}/{data}/{reservation_id} è stato contattato");
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        return reservationService.getReservationCheck(idLinea, dataFormatted, reservationId);
    }

    /**
     * Invia un oggetto JSON contenente il nome dell’alunno da trasportare, l’identificatore della fermata a cui sale/scende e il verso di percorrenza (andata/ritorno);
     * restituisce un identificatore univoco della reservation creata
     *
     * @param reservationResource JSON Body Reservation
     * @param idLinea             id linea
     * @param data                data in esame
     * @return identificatore univoco reservation
     */
    @PostMapping("/reservations/{id_linea}/{data}")
    public String postReservation(@RequestBody ReservationResource reservationResource, @PathVariable("id_linea") String idLinea, @PathVariable("data") String data) throws JsonProcessingException {
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        logger.info("Nuova Reservation " + reservationResource.toString());
        ReservationDTO reservationDTO = new ReservationDTO(reservationResource, lineeService.getLineaEntityById(idLinea).getId(), dataFormatted);
        String idReservation = reservationService.addReservation(reservationDTO);
        simpMessagingTemplate.convertAndSend("/reservation/" + data + "/" + idLinea + "/" + ((reservationResource.getVerso()) ? 1 : 0), reservationResource);
        return objectMapper.writeValueAsString(idReservation);
    }


    /**
     * Usato da guide linea per indicare che ha preso il bambino dalla fermata
     *
     * @param idLinea
     * @param verso
     * @param data
     * @param cfChild true per indicare che è stato preso, false per annullare
     */
    @PostMapping("/reservations/handled/{idLinea}/{verso}/{data}/{isSet}")
    public void manageHandled(@PathVariable("idLinea") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @PathVariable("isSet") Boolean isSet, @RequestBody String cfChild, HttpServletResponse response) throws Exception {
        Date datef = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        reservationService.manageHandled(verso, data, datef, isSet, idLinea, cfChild);
    }


    /**
     * Usato da guide linea per indicare che ha lasciato il bambino a scuola
     *
     * @param verso
     * @param data
     * @param cfChild
     * @param isSet   true per indicare che è arrivato, false per annullare
     */

    @PostMapping("/reservations/arrived/{idLinea}/{verso}/{data}/{isSet}")
    public void manageArrived(@PathVariable("verso") String idLinea, @PathVariable("verso") Boolean verso, @PathVariable("data") String data, @PathVariable("isSet") Boolean isSet, @RequestBody String cfChild) throws Exception {
        Date date = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        if (reservationService.manageArrived(verso, date, cfChild, isSet, idLinea))
            logger.info("Child " + cfChild + " is arrived");
    }

    /**
     * Invia un oggetto JSON che permette di aggiornare i dati relativi alla reservation indicata.
     * Il reservation_id ci permette di identificare la reservation da modificare, il body contiene i dati aggiornati.
     *
     * @param reservationResource JSON Body Reservation
     * @param idLinea             id linea
     * @param data                data in esame
     * @param reservationId       id Reservation da aggiornare
     */
    @PutMapping("/reservations/{id_linea}/{data}/{reservation_id}")
    public void updateReservation(@RequestBody ReservationResource reservationResource, @PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("Aggiornamento reservation " + reservationId);
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        ReservationDTO reservationDTO = new ReservationDTO(reservationResource, lineeService.getLineaEntityById(idLinea).getId(), dataFormatted);
        reservationService.updateReservation(reservationDTO, reservationId);
    }

    /**
     * Elimina la reservation indicata
     *
     * @param idLinea       id linea
     * @param data          data in esame
     * @param reservationId id reservation
     */
    @DeleteMapping("/reservations/{id_linea}/{data}/{reservation_id}")
    public void deleteReservation(@PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("reservation_id") ObjectId reservationId) {
        logger.info("Eliminazione reservation" + reservationId);
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        reservationService.deleteReservation(idLinea, dataFormatted, reservationId);
    }

    /**
     * Versione alternativa al metodo precedente
     *
     * @param codiceFiscale
     * @param idLinea
     * @param data
     * @param verso
     */
    @DeleteMapping("/reservations/{codiceFiscale}/{id_linea}/{data}/{verso}")
    public void deletePrenotazione(@PathVariable("codiceFiscale") String codiceFiscale, @PathVariable("id_linea") String idLinea, @PathVariable("data") String data, @PathVariable("verso") boolean verso) {
        logger.info("Elininazione prenotazione -> codice fiscale: " + codiceFiscale + ", data: " + data + ", verso: " + verso);
        Date dataFormatted = mongoTimeService.getMongoZonedDateTimeFromDate(data);
        reservationService.deleteReservation(codiceFiscale, dataFormatted, idLinea, verso);
    }


}
