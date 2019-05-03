package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        logger.info("login result -> " + userService.isLoginValid(userDTO));
        return null;
    }

    /**
     * Riceviamo un json con le sue nuove credenziali, le validiamo con un validator, se rispettano
     * i criteri lo registriamo e inviamo una mail per l'attivazione dell'account, se no restituiamo un errore
     *
     * @param userDTO
     * @param bindingResult
     */
    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().stream().forEach(err -> logger.error("/register -> " + err.toString()));
            return; //TODO
        }
        userService.registerUser(userDTO);
    }

    /**
     * Ci permette di abilitare l'account dopo che l'utente ha seguito l'url inviato per mail
     * @param randomUUID
     */
    @GetMapping("/confirm/{randomUUID}")
    public void confirm(@PathVariable("randomUUID") ObjectId randomUUID) {
        userService.enableUser(randomUUID);
    }

    /**
     * Riceviamo un’indirizzo di posta elettronica di cui si vuole ricuperare la
     * password. Se l’indirizzo corrisponde a quello di un utente registrato, invia un messaggio di
     * posta elettronica all’utente contenente un link random per la modifica della password.
     * Risponde sempre 200 - Ok
     */
    @PostMapping("/recover")
    public void recover(@RequestBody String email) {
        userService.recoverAccount(email);
    }

    /**
     * Verifica che il codice random:
     * - sia uno di quelli che abbiamo generato
     * - non sia scaduto. //TODO
     *
     * @param userDTO       come in /register
     * @param bindingResult
     * @param randomUUID
     * @Valid delle password
     * In caso vada tutto bene aggiorna la base dati degli utenti con la nuova password
     * return 200 – Ok, in caso negativo restituisce 404 – Not found
     */
    @PostMapping("/recover/{randomUUID}")
    public void recoverVerification(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult, @PathVariable("randomUUID") ObjectId randomUUID) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().stream().forEach(err -> logger.error("recover/randomUUID -> " + err.toString()));
            return; //TODO 404
        }

        try {
            userService.updateUserPassword(userDTO, randomUUID);
        } catch (UsernameNotFoundException e) {
            return; //TODO 404
        }

    }

}
