package it.polito.ai.mmap.esercitazione3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class AdminController {

    @GetMapping(value="/users", consumes={"text/html"})
    public String getUsers()
    {
        //todo realizzare la pagina html
        return "users";
    }

}
