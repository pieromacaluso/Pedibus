package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    /**
     * Un utente loggato recupera la lista dei suoi figli
     *
     * @return
     */
    @GetMapping("/children/")
    public List<ChildDTO> getMyChildren() {
        return userService.getMyChildren();
    }

    /**
     * L'utente loggato può aggiungere un suo figlio
     *
     * @param childDTO
     */
    @PostMapping("/children")
    public void registerChild(@RequestBody ChildDTO childDTO) {
        userService.registerChild(childDTO);
    }

    @DeleteMapping("children")
    public void delChild(@RequestBody String idChild) {
        userService.delChild(idChild);
    }

    /**
     * Permette ad un genitore di cambiare la fermata di default di uno dei figli
     *
     * @param childId
     * @param idFerm
     */
    @PutMapping("/children/updateChildStop/{childId}/{idFerm}")
    public void updateChildStop(@PathVariable String childId, @PathVariable String idFerm) {
        Integer idFermata = Integer.parseInt(idFerm);
        userService.updateChildStop(idFermata, childId);
    }
}
