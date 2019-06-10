package it.polito.ai.mmap.pedibus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.services.MongoZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * - una post a http://localhost:8080/debug/make genera:
 *
 *      - 100 Child
 *      - 50 user con 2 figli e pw = mail
 *      - 1 prenotazione/figlio per oggi, domani e dopo domani (o andata o ritorno)
 * - ci mette anche 2min a fare sta roba, ma controlla di non creare dupplicati ecc
 *
 */
@RestController
public class DebugController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ChildRepository childRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PrenotazioneRepository prenotazioneRepository;

    @Autowired
    FermataRepository fermataRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/debug/make")
    public void makeChildUserPrenotazioni() throws IOException {
        logger.info("POST /debug/make è stato contattato");

        List<ChildEntity> childList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/childEntity.json"), new TypeReference<List<ChildEntity>>() {
        });

        List<UserEntity> userList = userEntityListConverter();
        Iterator<ChildEntity> childEntityIterable = childList.iterator();

        int i = 0;
        while (childEntityIterable.hasNext()) {
            ChildEntity child1 = childEntityIterable.next();
            ChildEntity child2 = childEntityIterable.next();
            UserEntity parent = userList.get(i);
            parent.setChildrenList(new HashSet<>(Arrays.asList(child1.getCodiceFiscale(), child2.getCodiceFiscale())));
            Optional<UserEntity> checkDuplicate = userRepository.findByUsername(parent.getUsername());
            if (!checkDuplicate.isPresent()) {
                parent.setEnabled(true);
                parent = userRepository.save(parent); //per avere l'objectId

            }else
                parent = checkDuplicate.get();

            userList.set(i, parent);
            child1.setIdParent(parent.getId());
            child2.setIdParent(parent.getId());

            i++;
        }
        childRepository.saveAll(childList);
        logger.info("Child e User caricati");

        i = 0;
        for (int day = 0; day < 3; day++) {
            childEntityIterable = childList.iterator();
            while (childEntityIterable.hasNext()) {
                ChildEntity childEntity = childEntityIterable.next();
                int randFermata = (Math.abs(new Random().nextInt()) % 8) + 1; //la linea 1 ha 8 fermate
                PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity();
                prenotazioneEntity.setCfChild(childEntity.getCodiceFiscale());
                prenotazioneEntity.setData(MongoZonedDateTime.parseData("yyyy-MM-dd HH:mm z", LocalDate.now().plus(day, ChronoUnit.DAYS).toString() + " 12:00 GMT+00:00"));
                prenotazioneEntity.setIdFermata(randFermata);
                prenotazioneEntity.setNomeLinea("linea1");
                prenotazioneEntity.setVerso(randFermata < 5); //1-4 = true = andata
                if (!prenotazioneRepository.findByCfChildAndDataAndVerso(prenotazioneEntity.getCfChild(), prenotazioneEntity.getData(), randFermata < 5).isPresent())
                    prenotazioneRepository.save(prenotazioneEntity);
                i++;
            }
        }

        logger.info("Prenotazioni per oggi, domani e dopodomani caricate");
    }


    private List<UserEntity> userEntityListConverter() throws IOException {
        RoleEntity roleUser = roleRepository.findByRole("ROLE_USER");

        List<UserDTO> userList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/userDTO.json"), new TypeReference<List<UserDTO>>() {
        });
        return userList.stream().map(userDTO -> new UserEntity(userDTO, new HashSet<>(Arrays.asList(roleUser)), passwordEncoder)).collect(Collectors.toList());
    }

}
