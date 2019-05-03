package it.polito.ai.mmap.esercitazione3.controller;

import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.GMailSender;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class AuthenticationRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    UserService userService;

    @Autowired
    private GMailSender gMailSender;

    @PostMapping("/login")
    public String login() {
        return null;
    }


    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().stream().forEach(err -> logger.error(err.toString()));
            return;
        }
        userService.registerUser(userDTO);
        gMailSender.sendEmail(userDTO);
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
