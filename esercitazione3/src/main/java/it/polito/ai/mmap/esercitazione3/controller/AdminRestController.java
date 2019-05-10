package it.polito.ai.mmap.esercitazione3.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminRestController {

    @GetMapping(value="/users", consumes={"application/json","application/xml"})
    public String getUsers()
    {
        return "users";
    }

}
