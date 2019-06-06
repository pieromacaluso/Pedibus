package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    UserService userService;


    /**
     * L'utente loggato può aggiungere un suo figlio
     * @param childDTO
     */
    @PostMapping("/children/registerChild")
    public void registerChild(@RequestBody ChildDTO childDTO){
        userService.registerChild(childDTO);
    }

    @PutMapping("/children/updateChildStop/{childId}/{idFerm}")
    public void updateChildStop(@PathVariable String childId,@PathVariable String idFerm){
        Integer idFermata=Integer.parseInt(idFerm);
        userService.updateChildStop(idFermata,childId);
    }
}
