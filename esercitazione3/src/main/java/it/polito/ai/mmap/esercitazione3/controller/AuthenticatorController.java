package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


/**
 * Per restituire html relativi all'autenticazione, nel restController non si può (?)
 */
@Controller
public class AuthenticatorController {

    @Autowired
    private UserService userService;

    @GetMapping("/recover/{randomUUID}")
    public String recoverPage(@PathVariable("randomUUID") String randomUUID, Model model)
    {
        model.addAttribute("href", "/recover/" + randomUUID);
        return "recover";
    }
}
