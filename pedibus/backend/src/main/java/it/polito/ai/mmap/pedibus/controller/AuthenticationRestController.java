package it.polito.ai.mmap.pedibus.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.exception.RecoverProcessNotValidException;
import it.polito.ai.mmap.pedibus.exception.RegistrationNotValidException;
import it.polito.ai.mmap.pedibus.exception.TokenProcessException;
import it.polito.ai.mmap.pedibus.exception.UserNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.LoginUserDTO;
import it.polito.ai.mmap.pedibus.objectDTO.NewUserPassDTO;
import it.polito.ai.mmap.pedibus.objectDTO.RecoverUserDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.services.JwtTokenService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class AuthenticationRestController {
    @Autowired
    UserService userService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtTokenService jwtTokenService;
    @Autowired
    PasswordEncoder passwordEncoder;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * L'utente invia email e password, le validiamo e controlliamo se l'utente è attivo e la password è corretta.
     * In caso affermativo viene creato un json web token che viene ritornato all'utente, altrimenti restituisce 401-Unauthorized
     *
     * @param userDTO       Form Login Utente
     * @param bindingResult Validazione
     * @return Response Entity
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginUserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.getFieldErrorCount() == 0) {
            String username = userDTO.getEmail();
            String password = userDTO.getPassword();
            // Genera un 'Authentication' formato dall'user e password che viene poi autenticato.
            // In caso di credenziali errate o utente non abilitato sarà lanciata un'eccezione
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            } catch (AuthenticationException ignored) {
                throw new BadCredentialsException(PedibusString.LOGIN_FAILED);
            }
            logger.info(PedibusString.ENDPOINT_CALLED("POST", "/login " + userDTO.getEmail()));
            Map<Object, Object> model = new HashMap<>();
            model.put("token", userService.getJwtToken(username));
            return ok(model);
        } else {
            throw new BadCredentialsException(PedibusString.LOGIN_FAILED);
        }
    }

    /**
     * L'utente invia le sue nuove credenziali, le validiamo con un validator, se rispettano
     * i criteri lo registriamo e inviamo una mail per l'attivazione dell'account, se no restituiamo un errore
     *
     * @param userDTO       dati utente da registrare
     * @param bindingResult struttura per validazione
     * @deprecated Appartiene a vecchie esercitazioni, non più usato nel progetto finale
     */
    @Deprecated
    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/register " + userDTO.getEmail()));
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(err -> logger.error(err.toString()));
            throw new RegistrationNotValidException(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(",")));
        }
        userService.registerUser(userDTO);
    }

    /**
     * Controllo se la mail inserita presenta un duplicato
     *
     * @param email email da controllare
     * @return true se duplicata, falso altrimenti
     * @deprecated metodo utilizzato in passato dal modulo di registrazione ormai disattivato
     */
    @Deprecated
    @GetMapping("/register/checkMail/{email}")
    public boolean checkUserMailDuplicate(@PathVariable String email) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/register/checkMail/" + email));
        try {
            userService.loadUserByUsername(email);
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    /**
     * Ci permette di abilitare l'account dopo che l'utente ha seguito l'url inviato per mail
     *
     * @param randomUUID confirmation token
     * @deprecated La registrazione dell'utente avviene ora per vie differenti
     */
    @Deprecated
    @GetMapping("/confirm/{randomUUID}")
    public void confirm(@PathVariable("randomUUID") String randomUUID) {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/confirm/" + randomUUID));
        ObjectId id = new ObjectId(randomUUID);
        userService.enableUser(id);
    }

    /**
     * Riceviamo un’indirizzo di posta elettronica di cui si vuole ricuperare la
     * password. Se l’indirizzo corrisponde a quello di un utente registrato, invia un messaggio di
     * posta elettronica all’utente contenente un link random per la modifica della password.
     * Risponde sempre 200 - Ok
     *
     * @param email email specificata
     */
    @ApiOperation("Richiede l'invio della mail di recupero pw per l'account specificato")
    @PostMapping("/recover")
    public void recover(@RequestBody String email) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/recover " + email));
        try {
            userService.recoverAccount(email);
        } catch (UserNotFoundException ignored) {
            logger.error(PedibusString.RECOVER_MAIL_NOT_FOUND(email));
        }
    }

    /**
     * In caso vada tutto bene aggiorna la base dati degli utenti con la nuova password
     * return 200 – Ok, in caso negativo restituisce 404 – Not found
     *
     * @param userDTO       come login utente, ma contiene solo le due password da inserire per il recupero
     * @param bindingResult validazione dati
     * @param randomUUID    recover token
     */
    @ApiOperation("Permette di specificare le nuove credenziali per l'account nel processo di recupero pw")
    @PostMapping("/recover/{randomUUID}")
    public void recoverVerification(@ApiParam(name = "userDTO", value = "Le credenzialia aggiornate") @Valid @RequestBody RecoverUserDTO userDTO,
                                    BindingResult bindingResult,
                                    @ApiParam(name = "randomUUID", value = "Il token inviato per mail") @PathVariable("randomUUID") String randomUUID) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/recover/" + randomUUID));
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(err -> logger.error("recover/randomUUID -> " + err.toString()));
            throw new RecoverProcessNotValidException();
        }
        try {
            userService.updateUserPassword(userDTO, randomUUID);
        } catch (Exception e) {
            throw new RecoverProcessNotValidException();
        }
    }

    /**
     * In caso vada tutto bene aggiorna la base dati degli utenti con la nuova password abilitando l'utente.
     * return 200 – Ok, in caso negativo restituisce 404 – Not found
     *
     * @param newUserPassDTO Form creazione nuovo utente, primo accesso
     * @param bindingResult  Binding Result per verificare che i dati siano corretti
     * @param randomUUID     randomUUID relativo all'utente che deve cambiare password
     */
    @ApiOperation("Permette di attivare un account")
    @PostMapping("/new-user/{randomUUID}")
    public void newUserVerification(@ApiParam(name = "newuserPassDTO", value = "Contiene la password di default e quella nuova") @Valid @RequestBody NewUserPassDTO newUserPassDTO,
                                    BindingResult bindingResult,
                                    @ApiParam(name = "randomUUID", value = "Il token inviato per mail") @PathVariable("randomUUID") String randomUUID) {
        logger.info(PedibusString.ENDPOINT_CALLED("POST", "/new-user/" + randomUUID));
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(err -> logger.error("new-user/randomUUID -> " + err.toString()));
            throw new TokenProcessException(PedibusString.INVALID_DATA);
        }
        userService.updateNewUserPasswordAndEnable(newUserPassDTO, randomUUID);
    }
}
