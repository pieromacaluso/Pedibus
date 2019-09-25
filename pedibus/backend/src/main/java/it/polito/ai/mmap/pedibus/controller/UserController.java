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

    // todo probabilmente da cancellare, i bimbi vengono aggiunti tramite json (?)

//    /**
//     * L'utente loggato può aggiungere un suo figlio
//     *
//     * @param childDTO
//     */
//    @PostMapping("/children")
//    public void registerChild(@RequestBody ChildDTO childDTO) {
//        userService.registerChild(childDTO);
//    }
//
//    @DeleteMapping("children")
//    public void delChild(@RequestBody String idChild) {
//        userService.delChild(idChild);
//    }

    /**
     * Permette ad un genitore di cambiare le fermate di default di uno dei figli
     *
     * @param cfChild
     * @param sRes
     */
    @PutMapping("/children/stop/{childId}")
    public void updateChildStop(@PathVariable String cfChild, @RequestBody ChildDefaultStopResource stopRes) {
        userService.updateChildStop(cfChild, stopRes);
    }
}
