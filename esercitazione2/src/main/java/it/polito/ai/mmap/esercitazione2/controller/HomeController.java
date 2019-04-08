package it.polito.ai.mmap.esercitazione2.controller;

import it.polito.ai.mmap.esercitazione2.services.JsonHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    JsonHandlerService jsonHandlerService;

    // Mapping verso la home dell'applicazione
    @GetMapping("/")
    public String home() {
        jsonHandlerService.readPiedibusLines();
        return "home";
    }

    //Restituisce una lista JSON con i nomi delle lines presenti nel DBMS
    @GetMapping("/lines")
    public String getLines() {

        return "linesJSON";
    }

    //Restituisce un oggetto JSON contenente due liste, riportanti i dettagli delle fermate di andata e ritorno;
    @GetMapping("/lines/{nome_linea}")
    public String getStopsLine() {

        return "stopsJSON";
    }

    /*
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
