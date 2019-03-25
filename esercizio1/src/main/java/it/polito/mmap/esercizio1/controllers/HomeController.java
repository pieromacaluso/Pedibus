package it.polito.mmap.esercizio1.controllers;


import it.polito.mmap.esercizio1.viewModels.UserVM;
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
    private ConcurrentHashMap<String, UserVM> users;

    /*Metodo implementato per restituire la schermata home (in questo caso quella di login che ha il link per il redirect a quella di registrazione)
     * */
    @GetMapping("/")
    public String home() {
        return "login";
    }

    /*Metodo usato per l'apertura della pagina tramite richiesta get.
     * Tramite l'annotazione @ModelAttribute("uservm") viene creato un oggetto UserVM usato dal form html tramite il campo th:object="${userVM}".
     * E' necessario per poter generare i messaggi di errore per ogni singolo campo del form stesso.
     * */
    @GetMapping("/register")
    public String viewFormRegistration(@ModelAttribute("uservm") UserVM uvm, Model m) {
        return "register";
    }


    /*Metodo usato per elaborare il form compilato dall'utente.
     * la validazione sui campi è fatta tramite i vincoli scritti in UserVM.
     * Gli errori sono riportati al client grazie agli appositi div che controllano se ci sono errori di validazione per ogni campo del form
     * la registrazione consiste in un semplice inserimento in lista, non bisogna gestire le sessioni per questo lab
     * Per questo lab la password è salvata in chiaro.
     * */
    @PostMapping("/register")
    public String processForm(@Valid @ModelAttribute("uservm") UserVM uvm, BindingResult res, Model m) {
        //Per stampare tutti gli errori prodotti dal processo di validazione
        //res.getAllErrors().stream().forEach(err -> logger.info(err.getDefaultMessage()));

        //Match password code
        //probabilmente non è la soluzione migliore, ma in thymeleaf non riesco ad accedere ai GlobalError che sono quegli errori derivanti da annotazioni di classe
        if (res.hasGlobalErrors()) {
            for (ObjectError err : res.getGlobalErrors()) {
                //todo bisogna capire come controllare che sia l'errore che vogliamo noi, per adesso va bene comunque avendone solo uno
                //if (err.contains(ConstraintValidator < FieldsValueMatch, err >))
                uvm.setPassMatchError(err.getDefaultMessage());
            }
            return "register";
        } else {
            //Registrazione utente

            //non esistente
            users.put(uvm.getEmail(), uvm);          //inserimento in elenco del nuovo utente
            m.addAttribute("message", uvm.getEmail() + " registrato correttamente");
            logger.info(uvm.getEmail() + " registrato correttamente.size map post insert: " + users.size());
            return "privatehome";                   //pagina per mostrare la parte privata
        }

    }


    /*Metodo usato per l'apertura della pagina tramite richiesta get.
     * Tramite l'annotazione @ModelAttribute("uservm") viene creato un oggetto UserVM usato dal form html tramite il campo th:object="${userVM}".
     * E' necessario per poter generare i messaggi di errore per ogni singolo campo del form stesso.
     * */
    @GetMapping("/login")
    public String login(@ModelAttribute("uservmLogin") UserVM uvm) {
        return "login";
    }

    //todo sistemare html per usare un oggetto nel form come in fase di registrazione
    /*Metodo per implementare il meccanismo di login.
    Basato sul controllo dell'elenco degli utenti iscritti e del confronto della password.
    In caso di errori si riporta la pagina di login con l'errore,altrimenti si ha una redirect sulla pagina privata
    * */
    /*@PostMapping("/login")
    public String loginForm(@Valid @ModelAttribute("uservmLogin") UserVM uvm , BindingResult res, Model m){
        if(res.hasErrors()){
            m.addAttribute("message","Errori validation.");
            return "login";
        }else{
            if(users.containsKey(uvm.getEmail())){
                //utente registrato
                UserVM u=users.get(uvm.getEmail());
                if(uvm.getPass().compareTo(u.getPass())==0){
                    //pass corretta
                    return "privatehome";
                }else{
                    m.addAttribute("message","Password errata.");
                    return "login";
                }
            }else{
                //utente non registrato
                m.addAttribute("message","Utente non registrato.");
                return "login";
            }
        }

    }*/

    /*Metodo usato per evitare che la risorsa venga richiesta tramite url
     * si esegue una redirect sulla pagina di login
     * UserVM come parametro per permettere la realizzazione della pagina di login
     * */
    @GetMapping("/privatehome")
    public String privateHome(@ModelAttribute("uservm") UserVM uvm) {
        return "login";
    }

}
