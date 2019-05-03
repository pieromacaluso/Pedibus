package it.polito.ai.mmap.esercitazione3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Per restituire html relativi all'autenticazione, nel restController non si può (?)
 */
@Controller
public class AuthenticatorController {

    @GetMapping("/recover/randomUUID")
    public void recoverPage()
    {

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
