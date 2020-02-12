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
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;

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
        dataCreationService.deleteAll();
    }


    /**
     * Si può chiamare ripetutamente e creerà nuovi dati
     * Genera:
     * - countCreate genitori, ognuno con due figli
     * - 2*countCreate guide
     * - 1 admin della linea 1
     * - 1 admin della linea 2
     *
     * @param countCreate numero di oggetti
     * @throws IOException Eccezione lettura da file
     */
    @GetMapping("/debug/make/{countCreate}")
    public void makeChildUser(@PathVariable("countCreate") int countCreate) throws IOException {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/debug/make/" + countCreate));
        dataCreationService.makeChildUser(countCreate);
    }

    /**
     * Si può chiamare ripetutamente e creerà nuovi dati
     * Genera:
     * - countCreate genitori, ognuno con due figli
     *
     * @throws IOException Eccezione lettura da file
     */
    @GetMapping("/debug/make/child/{countCreate}")
    public void makeChild(@PathVariable("countCreate") int countCreate) throws IOException {
        logger.info(PedibusString.ENDPOINT_CALLED("GET", "/debug/make/child/" + countCreate));
        dataCreationService.makeChild(countCreate);
    }

    /**
     * Unisce il json dei bimbi (childEntity_base.json) alla lista di cf (cf.txt)
     *
     * @return
     * @throws IOException
     */
    @GetMapping("/debug/transform")
    public List<ChildEntity> debug() throws IOException {
        return dataCreationService.transform();
    }
}
