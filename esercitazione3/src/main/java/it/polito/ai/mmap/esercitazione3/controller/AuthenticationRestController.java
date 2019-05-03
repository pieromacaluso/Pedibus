package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class AuthenticationRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody UserDTO userDTO) {
        logger.info("login  "+userService.isLoginValid(userDTO));
        return null;
    }

    /**
     * L'utente invia un json con le sue nuove credenziali, le validiamo con un validator, se rispettano
     * i criteri lo registriamo e inviamo una mail per l'attivazione dell'account, se no restituiamo un errore
     * @param userDTO
     * @param bindingResult
     */
    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().stream().forEach(err -> logger.error(err.toString()));
            return; //TODO
        }
        userService.registerUser(userDTO);
    }

    @GetMapping("/confirm/{randomUUID")
    public void confirm(@PathVariable("randomUUID") String randomUUID) {

    }

    @PostMapping("/recover")
    public void recover() {

    }

    @PostMapping("/recover/{randomUUID}")
    public void recoverVerification() {

    }

}
