package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.resources.ChildDefaultStopResource;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    /**
     * Tramite questo endpoint un utente loggato recupera la lista dei suoi figli
     *
     * @return
     */
    @GetMapping("/children")
    public List<ChildDTO> getMyChildren() {
        return userService.getMyChildren();
    }


    /**
     * Permette ad un genitore di cambiare le fermate di default di uno dei figli
     *
     * @param cfChild
     * @param stopRes
     */
    @PutMapping("/children/stops/{childId}")
    public void updateChildStop(@PathVariable String cfChild, @RequestBody ChildDefaultStopResource stopRes) {
        userService.updateChildStop(cfChild, stopRes);
    }
}
