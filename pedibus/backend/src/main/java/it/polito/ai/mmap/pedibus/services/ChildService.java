package it.polito.ai.mmap.pedibus.services;


import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.*;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.ChildDefaultStopResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChildService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    LineeService lineeService;


    public ChildDTO getChildDTOById(String codiceFiscale) {
        Optional<ChildEntity> checkChild = childRepository.findById(codiceFiscale);
        if (checkChild.isPresent())
            return new ChildDTO(checkChild.get());
        else
            throw new ChildNotFoundException("Alunno non trovato");
    }

    public List<ChildDTO> getAllChildren() {
        return childRepository.findAll().stream().map(ChildDTO::new).collect(Collectors.toList());
    }

    /**
     * Recuperiamo da db i figli dell'utente loggato
     *
     * @return
     */
    public List<ChildDTO> getMyChildren() {
        UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((List<ChildEntity>) childRepository.findAllById(principal.getChildrenList())).stream().map(ChildDTO::new).collect(Collectors.toList());
    }

    /**
     * Metodo che permette di cambiare la fermata di default di un bambino o dal suo genitore o da un System-Admin
     *
     * @param cfChild
     * @param stopRes
     */
    public void updateChildStop(String cfChild, ChildDefaultStopResource stopRes) {
        Optional<ChildEntity> c = childRepository.findById(cfChild);
        if (c.isPresent()) {
            UserEntity principal = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.getChildrenList().contains(cfChild) || principal.getRoleList().contains(userService.getRoleEntityById("ROLE_SYSTEM-ADMIN"))) {
                ChildEntity childEntity = c.get();

                lineeService.getFermataEntityById(stopRes.getIdFermataAndata());
                childEntity.setIdFermataAndata(stopRes.getIdFermataAndata());
                lineeService.getFermataEntityById(stopRes.getIdFermataRitorno());
                childEntity.setIdFermataRitorno(stopRes.getIdFermataRitorno());

                childRepository.save(childEntity);

            } else
                throw new ChildNotFoundException("Bambino non trovato tra i tuoi figli");
        } else
            throw new ChildNotFoundException("Bambino non trovato");
    }
}
