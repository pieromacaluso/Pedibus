package it.polito.ai.mmap.esercitazione3.controller;


import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

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
    @GetMapping(value="/users", consumes={"application/json","application/xml"})    //tramite consumers è possibile indicare quale header Content-Type deve avere la richiesta
    public ResponseEntity getUsers()
    {
        List<UserEntity> allUsers=userService.getAllUsers();
        Map<Object, Object> model = new HashMap<>();
        model.put("ListaUtenti", allUsers);
        return ok(model);
    }

    //todo da finire
    @PutMapping("/users/{userID}")
    public ResponseEntity setUserAdmin(@RequestBody String nomeLinea, @PathVariable("userID") String userID){
        UserEntity userEntity;
        //Collection<? extends GrantedAuthority> roles= SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        try {
            userEntity= (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        }catch (Exception e){
            //todo il getPrincipal ritorna un Object. Tale metodo per
        }
        return (ResponseEntity) ok();
    }

}
