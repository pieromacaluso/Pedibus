package it.polito.mmap.esercizio1.controllers;


import it.polito.mmap.esercizio1.viewModels.FormUserLogin;
import it.polito.mmap.esercizio1.viewModels.FormUserRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class HomeController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ConcurrentHashMap<String, FormUserRegistration> users;


    /**
     * Metodo implementato per restituire la schermata home
     * (in questo caso quella di login che ha il link per il redirect a quella di registrazione)
     *
     * @return String "login"
     */
    @GetMapping("/")
    public String home(@ModelAttribute("formUserLogin") FormUserLogin uvm) {
        return "login";
    }

    /**
     * Metodo usato per l'apertura della pagina tramite richiesta get.
     * Tramite l'annotazione @ModelAttribute("formUserRegistration") viene creato un oggetto FormUserRegistration usato dal form html
     * tramite il campo th:object="${userVM}".
     * E' necessario per poter generare i messaggi di errore per ogni singolo campo del form stesso.
     *
     * @param uvm FormUserRegistration object
     * @param m   Model
     * @return String "register"
     */
    @GetMapping("/register")
    public String viewFormRegistration(@ModelAttribute("formUserRegistration") FormUserRegistration uvm, Model m) {
        return "register";
    }

    /**
     * Metodo usato per elaborare il form compilato dall'utente.
     * la validazione sui campi è fatta tramite i vincoli scritti in FormUserRegistration.
     * Gli errori sono riportati al client grazie agli appositi div che controllano
     * se ci sono errori di validazione per ogni campo del form
     * la registrazione consiste in un semplice inserimento in lista, non bisogna gestire le sessioni per questo lab
     * Per questo lab la password è salvata in chiaro.
     *
     * @param uvm FormUserRegistration object
     * @param res BindingResult
     * @param m   Model
     * @return String
     */
    @PostMapping("/register")
    public String processForm(@Valid @ModelAttribute("formUserRegistration") FormUserRegistration uvm, BindingResult res, Model m) {
        //Per stampare tutti gli errori prodotti dal processo di validazione
        //res.getAllErrors().stream().forEach(err -> logger.info(err.getDefaultMessage()));

        if (!res.hasErrors()) {
            //Registrazione utente non esistente
            users.put(uvm.getEmail(), uvm);          //inserimento in elenco del nuovo utente
//            m.addAttribute("message", uvm.getEmail() + " registrato correttamente");
            // Metto tra attributi FormUserRegistration per visualizzare dati a video
            logger.info(uvm.getEmail() + " registrato correttamente. Size map post insert: " + users.size());
            m.addAttribute("user", uvm);
            return "privatehome";                   //pagina per mostrare la parte privata
        } else {
            return "register";
        }
    }


    /**
     * Metodo usato per l'apertura della pagina tramite richiesta get.
     * Tramite l'annotazione @ModelAttribute("formUserRegistration") viene creato un oggetto FormUserRegistration
     * usato dal form html tramite il campo th:object="${userVM}".
     * E' necessario per poter generare i messaggi di errore per ogni singolo campo del form stesso.
     *
     * @param uvm FormUserRegistration object
     * @return String
     */
    @GetMapping("/login")
    public String login(@ModelAttribute("formUserLogin") FormUserLogin uvm) {
        return "login";
    }

    /**
     * Metodo per implementare il meccanismo di login.
     * Basato sul controllo dell'elenco degli utenti iscritti e del confronto della password.
     * In caso di errori si riporta la pagina di login con l'errore, altrimenti si ha una redirect
     * sulla pagina privata
     *
     * @param uvm FormUserLogin object
     * @param res BindingResult
     * @param m   Model
     * @return String
     */
    @PostMapping("/login")
    public String loginForm(@Valid @ModelAttribute("formUserLogin") FormUserLogin uvm, BindingResult res, Model m) {
        if (!res.hasErrors() && uvm.getPass().equals(users.get(uvm.getEmail()).getPass())) {
            // Login Corretto
            logger.info(uvm.getEmail() + " ha effettuato il login correttamente.");
            m.addAttribute("user", users.get(uvm.getEmail()));
            return "privatehome";
        } else {
            // Login errato
            /*
                Aggiunge un ulteriore errore globale per annunciare incorrettezza dei dati di accesso.
                Non si mostrano gli errori dei campi per fare in modo di mantenere intatta la sicurezza
                del form di login. Si lascia per qualsiasi tipologia di errore un errore generico che non permette
                di inferire la natura del vero errore
             */
            res.addError(new ObjectError("formUserLogin", "Email and/or Password are incorrect"));
            return "login";
        }
    }

    /**
     * Metodo usato per evitare che la risorsa venga richiesta tramite url
     * si esegue una redirect sulla pagina di login
     * FormUserRegistration come parametro per permettere la realizzazione della pagina di login
     *
     * @param uvm FormUserRegistration object
     * @return String
     */
    @GetMapping("/privatehome")
    public String privateHome(@ModelAttribute("uservm") FormUserRegistration uvm) {
        return "login";
    }

}
