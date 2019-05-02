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
    public String login() {
        return null;
    }


    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            bindingResult.getAllErrors().stream().forEach(err -> logger.error(err.toString()));
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
