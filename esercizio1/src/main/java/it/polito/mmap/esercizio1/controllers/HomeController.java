package it.polito.mmap.esercizio1.controllers;


import it.polito.mmap.esercizio1.viewModels.UserVM;
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
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class HomeController {

    @Autowired
    private ConcurrentHashMap<String,UserVM> users;

@GetMapping("/register")
public String processForm(@Valid UserVM vm, BindingResult res, Model m){
    //logger.info(vm.toString());


    if(res.hasErrors()) {
        /*FieldError err=res.getFieldError("first");
        if(err!=null){

        }*/


        m.addAttribute("first","ciao");
    }
        /*if(users.containsKey(vm.getEmail())){
            //esiste
            m.addAttribute("message","User "+ vm.getEmail()+" non disponibile");
        }else{
            //non esiste
            users.put(vm.getEmail(),vm);
            m.addAttribute("message","User "+ vm.getEmail()+" registrato");
        }*/
    return "register";
}

}
