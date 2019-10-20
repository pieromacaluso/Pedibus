package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class AdminRestController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Environment environment;
    @Autowired
    UserService userService;
    @Autowired
    ChildService childService;
    @Autowired
    LineeService lineeService;


    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti gli utenti registrati.
     * Essendo usato da admin può essere più utile restituire le UserEntity e non UserDTO per poter osservare
     * maggior dettagli quali ruoli e altro.
     * TODO (marcof) non mi convince troppo restituire un entity perchè dovrebbe funzionare solo come oggetto tramite per il db, c'è da capire che dati gli possono servire e metterli nel dto (?)
     *
     * @return
     */
    @GetMapping("/admin/users")
    //tramite consumers è possibile indicare quale header Content-Type deve avere la richiesta
    public ResponseEntity getUsers() {
        List<UserEntity> allUsers = userService.getAllUsers();
        Map<Object, Object> model = new HashMap<>();
        model.put("ListaUtenti", allUsers);
        return ok(model);
    }

    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti i bambini
     *
     * @return
     */
    @GetMapping("/admin/children/")
    public List<ChildDTO> getChildren() {
        return childService.getAllChildren();
//        List<ChildDTO> allChildren=userService.getAllChildren();
//        Map<Object, Object> model = new HashMap<>();
//        model.put("ListaChildren", allChildren);
//        return ok(model);
    }

    /**
     * Un admin di una linea o il system-admin inserisce un utente come admin per una linea, indicando tramite PermissionResource.addOrDel se aggiungere(true) o eliminare(false) il permesso
     * Questo utente può essere già registrato o no e quando passerà attraverso il processo di registrazione si troverà i privilegi di admin
     */
    @PutMapping("/admin/users/{userID}")
    public void setUserAdmin(@RequestBody PermissionResource permissionResource, @PathVariable("userID") String userID) {
        if (lineeService.isAdminLine(permissionResource.getIdLinea()) || userService.isSysAdmin()) {
            if (permissionResource.isAddOrDel()) {
                userService.addAdmin(userID);
                lineeService.addAdminLine(userID, permissionResource.getIdLinea());
            } else {
                userService.delAdmin(userID);
                lineeService.delAdminLine(userID, permissionResource.getIdLinea());
            }
        } else {
            throw new PermissionDeniedException("Non hai i privilegi per eseguire questa operazione");
        }

    }


}
