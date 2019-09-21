package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.PermissionDTO;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class AdminRestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;
    @Autowired
    LineeService lineeService;
    @Autowired
    RoleRepository roleRepository;


    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti gli utenti registrati.
     * Essendo usato da admin può essere più utile restituire le UserEntity e non UserDTO per poter osservare
     * maggior dettagli quali ruoli e altro.
     * TODO (marcof) non mi convince troppo restituire un entity perchè dovrebbe funzionare solo come oggetto tramite per il db, c'è da capire che dati gli possono servire e metterli nel dto (?)
     * @return
     */
    @GetMapping(value = "/admin/users")
    //tramite consumers è possibile indicare quale header Content-Type deve avere la richiesta
    public ResponseEntity getUsers() {
        List<UserEntity> allUsers = userService.getAllUsers();
        Map<Object, Object> model = new HashMap<>();
        model.put("ListaUtenti", allUsers);
        return ok(model);
    }

    /**
     * Un admin di una linea o il system-admin inserisce @param userID come admin per @param nomeLinea, indica tramite @addOrDel se aggiungere(true) o eliminare(false) il permesso
     * Questo utente può essere già registrato o no e quando passerà attraverso il processo di registrazione si troverà i privilegi di admin
     */
    @PutMapping("/admin/users/{userID}")
    public void setUserAdmin(@RequestBody PermissionDTO permissionDTO, @PathVariable("userID") String userID) {
        UserEntity principal;
        LineaDTO lineaDTO = lineeService.getLineById(permissionDTO.getLinea());

        principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (lineaDTO.getAdminList().contains(principal.getUsername()) || principal.getRoleList().contains(roleRepository.findByRole("ROLE_SYSTEM-ADMIN"))) {
                if(permissionDTO.isAddOrDel()){
                    userService.addAdmin(userID);
                    lineeService.addAdminLine(userID, permissionDTO.getLinea());
                }else{
                    userService.delAdmin(userID);
                    lineeService.delAdminLine(userID, permissionDTO.getLinea());
                }
        } else {
            throw new PermissionDeniedException("Non hai i privilegi per eseguire questa operazione");
        }

    }

    /**
     * Metodo usato solo da Admin o System-Admin per avere l'elenco di tutti i bambini
     * @return
     */
    @GetMapping("/admin/children/")
    public List<ChildDTO> getChildren(){
        return userService.getAllChildren();
//        List<ChildDTO> allChildren=userService.getAllChildren();
//        Map<Object, Object> model = new HashMap<>();
//        model.put("ListaChildren", allChildren);
//        return ok(model);
    }

}
