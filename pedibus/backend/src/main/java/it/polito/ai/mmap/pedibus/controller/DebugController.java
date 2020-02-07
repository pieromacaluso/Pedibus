package it.polito.ai.mmap.pedibus.controller;


import it.polito.ai.mmap.pedibus.configuration.PedibusString;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.repository.ChildRepository;
import it.polito.ai.mmap.pedibus.repository.ReservationRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.services.DataCreationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Profile("dev")
@RestController
@ApiIgnore
public class DebugController {

    @Autowired
    ChildRepository childRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    UserService userService;
    @Autowired
    DataCreationService dataCreationService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Endpoint di debug, cancella tutto dal database
     */
    @GetMapping("/debug/delete")
    public void deleteAll() {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/debug/delete"));
        reservationRepository.deleteAll();
        userRepository.deleteAll(userRepository.findAll().stream().filter(userEntity -> !userEntity.getRoleList().contains(userService.getRoleEntityById("ROLE_SYSTEM-ADMIN"))).collect(Collectors.toList()));
        childRepository.deleteAll();
        logger.info(PedibusString.ALL_DELETED);
    }


    /**
     * Si può chiamare ripetutamente e creerà nuovi dati
     * Genera:
     * - countCreate genitori, ognuno con due figli
     * - 2*countCreate guide
     * - 1 admin della linea 1
     * - 1 admin della linea 2
     *
     * @throws IOException Eccezione lettura da file
     */
    @GetMapping("/debug/make/{countCreate}")
    public void makeChildUser(@PathVariable("countCreate") int countCreate) throws IOException {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/debug/make/" + countCreate));
        dataCreationService.makeChildUser(countCreate);
    }

    @PostMapping("/debug/transform")
    public List<ChildEntity> debug() throws IOException {
        return dataCreationService.transform();
    }
}
