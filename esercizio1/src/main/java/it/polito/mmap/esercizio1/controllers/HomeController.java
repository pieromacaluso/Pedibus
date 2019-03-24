package it.polito.mmap.esercizio1.controllers;


import it.polito.mmap.esercizio1.customValidators.FieldsValueMatch;
import it.polito.mmap.esercizio1.viewModels.UserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.ConstraintValidator;
import javax.validation.Valid;
import java.util.HashMap;
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
     * */
    @PostMapping("/register")
    public String processForm(@Valid @ModelAttribute("uservm") UserVM uvm, BindingResult res, Model m) {

        //Match password code
        //probabilmente non è la soluzione migliore, ma in thymeleaf non riesco ad accedere ai GlobalError che sono quegli errori derivanti da annotazioni di classe
        if (res.hasGlobalErrors()) {
            for (ObjectError err : res.getGlobalErrors()) {
                //TO-DO bisogna capire come controllare che sia l'errore che vogliamo noi, per adesso va bene comunque avendone solo uno
                //if (err.contains(ConstraintValidator < FieldsValueMatch, err >))
                uvm.setPassMatchError(err.getDefaultMessage());
            }
        }

        return "register";
    }

}
