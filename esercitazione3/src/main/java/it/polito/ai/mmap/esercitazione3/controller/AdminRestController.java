package it.polito.ai.mmap.esercitazione3.controller;


import it.polito.ai.mmap.esercitazione3.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.MongoService;
import it.polito.ai.mmap.esercitazione3.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class AdminRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;
    @Autowired
    MongoService mongoService;


    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti gli utenti registrati.
     * Essendo usato da admin può essere più utile restituire le UserEntity e non UserDTO per poter osservare
     * maggior dettagli quili ruoli e altro.
     *
     * @return
     */
    @GetMapping(value = "/users", consumes = {"application/json", "application/xml"})
    //tramite consumers è possibile indicare quale header Content-Type deve avere la richiesta
    public ResponseEntity getUsers() {
        List<UserEntity> allUsers = userService.getAllUsers();
        Map<Object, Object> model = new HashMap<>();
        model.put("ListaUtenti", allUsers);
        return ok(model);
    }

    @PutMapping("/users/{userID}")
    public void setUserAdmin(@RequestBody String nomeLinea, @PathVariable("userID") String userID) {
        UserEntity principal;
        LineaDTO lineaDTO = mongoService.getLineByName(nomeLinea);        //todo vedere se gestisce le eccezioni come loadUserById

        // try {
        principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.getRoleList().stream().map(RoleEntity::getRole).collect(Collectors.toList()).contains("ROLE_SYSTEM-ADMIN") || lineaDTO.getAdminList().contains(principal.getUsername())) {
                userService.addAdmin(userID);
                mongoService.addAdminLine(userID, nomeLinea);
        } else {
            logger.info("else");
            //todo eccezione
        }

        //}catch (Exception e){
        //todo il getPrincipal ritorna un Object che potrebbe non sempre essere convertito in entity. Essendo però chiamato solo se autenticto non si dovrebbero avere problemi. Da verificare per bene
        //}
        return;
    }

}
