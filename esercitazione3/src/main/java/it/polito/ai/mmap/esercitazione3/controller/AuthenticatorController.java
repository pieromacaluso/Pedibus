package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * Per restituire html relativi all'autenticazione, nel restController non si può (?)
 */
@Controller
public class AuthenticatorController {

    @Autowired
    private UserService userService;

    @GetMapping("/recover/randomUUID")
    public String recoverPage(@PathVariable("randomUUID") String randomUUID)
    {
        // TODO: settare in un campo della pagina il valore del token
        return "recover";
    }

    @GetMapping("/users")
    public void getUsers()
    {

    }

    @PutMapping("/users/{userID}")
    public void addLineAdmin()
    {

    }
}
