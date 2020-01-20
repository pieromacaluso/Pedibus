package it.polito.ai.mmap.pedibus.controller;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.exception.PermissionDeniedException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.repository.RoleRepository;
import it.polito.ai.mmap.pedibus.resources.UserInsertResource;
import it.polito.ai.mmap.pedibus.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    @Autowired
    RoleRepository roleRepository;

    /**
     * @return i possibili ruoli da assegnare a un utente
     */
    @GetMapping("/sysadmin/roles")
    public List<String> getAvailableRole() {
        return userService.getAllRole();
    }

    /**
     * @param userInsertResource l'utente da inserire con i suoi ruoli
     */
    @PostMapping("/sysadmin/user")
    public void insertUser(@RequestBody UserInsertResource userInsertResource) {
        //todo controllo validità mail
        userService.insertUser(userInsertResource);
    }

    /**
     * @return l'elenco degli utenti salvati
     */
    @GetMapping("/sysadmin/users")
    public Page<UserInsertResource> getUsers(Pageable pageable, @RequestParam("keyword") String keyword) {
        return userService.getAllPagedUsers(pageable, keyword);
    }
    /**
     * @param userInsertResource l'utente i cui dettagli vanno aggiornati
     */
    @PutMapping("/sysadmin/users/{userId}")
    public void updateUser(@PathVariable("userId") String userId, @RequestBody UserInsertResource userInsertResource) {
        userService.updateUser(userId, userInsertResource);
    }

    /**
     * @return l'elenco degli utenti salvati
     */
    @DeleteMapping("/sysadmin/users/{userId}")
    public void deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUserByUsername(userId);
    }

    /**
     * @return l'elenco di tutti i bambini salvati
     */
    @GetMapping("/sysadmin/children")
    public Page<ChildDTO> getChildren(Pageable pageable, @RequestParam("keyword") String keyword) {
        return childService.getAllPagedChildren(pageable, keyword);
    }

    /**
     * @return l'elenco di tutti i bambini salvati
     */
    @PostMapping("/sysadmin/children")
    public ChildDTO createChild(@RequestBody ChildDTO childDTO) {
        ChildEntity childEntity = childService.createChild(childDTO);
        return new ChildDTO(childEntity);
    }
    @PutMapping("/sysadmin/children/{childId}")
    public ChildDTO updateChild(@PathVariable("childId") String childId, @RequestBody ChildDTO childDTO) {
        ChildEntity childEntity = childService.updateChild(childId, childDTO);
        return new ChildDTO(childEntity);
    }
    @DeleteMapping("/sysadmin/children/{childId}")
    public void deleteChild(@PathVariable("childId") String childId) {
        childService.deleteChild(childId);
    }

    /**
     * @return l'elenco di tutti i bambini salvati
     */
    @GetMapping("/sysadmin/children/{idChild}")
    public ChildDTO getChildren(@PathVariable("idChild") String cfChild) {
        return childService.getChildDTOById(cfChild);
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
