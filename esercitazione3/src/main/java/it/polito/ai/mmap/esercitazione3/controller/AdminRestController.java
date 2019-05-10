package it.polito.ai.mmap.esercitazione3.controller;


import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class AdminRestController {

    @Autowired
    UserService userService;


    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti gli utenti registrati.
     * Essendo usato da admin può essere più utile restituire le UserEntity e non UserDTO per poter osservare
     * maggior dettagli quili ruoli e altro.
     * @return
     */
    @GetMapping(value="/users", consumes={"application/json","application/xml"})
    public ResponseEntity getUsers()
    {
        List<UserEntity> allUsers=userService.getAllUsers();
        Map<Object, Object> model = new HashMap<>();
        model.put("ListaUtenti", allUsers);
        return ok(model);
    }

}
