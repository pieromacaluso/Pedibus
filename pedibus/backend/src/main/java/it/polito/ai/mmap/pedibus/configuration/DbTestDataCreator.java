package it.polito.ai.mmap.pedibus.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.services.MongoZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbTestDataCreator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    LineaRepository lineaRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PrenotazioneRepository prenotazioneRepository;
    @Autowired
    FermataRepository fermataRepository;

    @Autowired
    private Environment environment;

    /**
     * crea:
     * - 100 Child
     * - 50 genitori con 2 figli        contenuti nel file genitori.json e pw = 1!qwerty1!
     * - 25 nonni admin della linea1    contenuti nel file nonni_linea1.json e pw = 1!qwerty1!
     * - 25 nonni admin della linea2    contenuti nel file nonni_linea2.json e pw = 1!qwerty1!
     * - 1 prenotazione/figlio per oggi, domani e dopo domani (o andata o ritorno)
     */
    public void makeChildUserPrenotazioni() throws IOException {
        prenotazioneRepository.deleteAll();
        int count = 0;
        RoleEntity roleUser = roleRepository.findByRole("ROLE_USER");
        RoleEntity roleAdmin = roleRepository.findByRole("ROLE_ADMIN");


        List<ChildEntity> childList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/childEntity.json"), new TypeReference<List<ChildEntity>>() {
        });

        List<UserEntity> userList = userEntityListConverter("genitori.json", roleUser);
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
                count++;
            } else
                parent = checkDuplicate.get();

            userList.set(i, parent);
            child1.setIdParent(parent.getId());
            child2.setIdParent(parent.getId());

            i++;
        }
        childRepository.saveAll(childList);
        logger.info(count + " genitori caricati");


        count = 0;
        LinkedList<UserEntity> listNonni = new LinkedList<>();
        for (i = 1; i <= 2; i++) {
            for (UserEntity nonno : userEntityListConverter("nonni_linea" + i + ".json", roleAdmin)) {
                Optional<UserEntity> checkDuplicate = userRepository.findByUsername(nonno.getUsername());
                if (!checkDuplicate.isPresent()) {
                    nonno.setRoleList(new HashSet<>(Arrays.asList(roleAdmin)));
                    nonno.setEnabled(true);
                    listNonni.add(nonno);
                    Optional<LineaEntity> check = lineaRepository.findByNome("linea" + i);

                    if (check.isPresent()) {
                        LineaEntity lineaEntity = check.get();
                        lineaEntity.getAdminList().add(nonno.getUsername());
                        lineaRepository.save(lineaEntity);
                    }
                    count++;
                }
            }
        }
        userRepository.saveAll(listNonni);
        logger.info(count + " nonni caricati");

        i = 0;
        count = 0;
        List<PrenotazioneEntity> prenotazioniList = new LinkedList<>();
        PrenotazioneEntity prenotazioneEntity;
        int randLinea;
        int randFermata;
        for (int day = 0; day < 3; day++) {
            childEntityIterable = childList.iterator();
            while (childEntityIterable.hasNext()) {
                ChildEntity childEntity = childEntityIterable.next();
                randFermata = (Math.abs(new Random().nextInt()) % 8) + 1; //la linea 1 ha 8 fermate
                randLinea = (Math.abs(new Random().nextInt()) % 2) + 1; //linea 1 o 2
                prenotazioneEntity = new PrenotazioneEntity();
                prenotazioneEntity.setCfChild(childEntity.getCodiceFiscale());
                prenotazioneEntity.setData(MongoZonedDateTime.getMongoZonedDateTimeFromDate(LocalDate.now().plus(day, ChronoUnit.DAYS).toString()));
                prenotazioneEntity.setIdFermata(randFermata + (100 * (randLinea - 1)));
                prenotazioneEntity.setNomeLinea("linea" + randLinea);
                prenotazioneEntity.setVerso(randFermata < 5); //1-4 = 101-104 = true = andata

                if (!prenotazioneRepository.findByCfChildAndData(prenotazioneEntity.getCfChild(), prenotazioneEntity.getData()).isPresent()) {
                    prenotazioniList.add(prenotazioneEntity);
                    count++;
                }
                i++;
            }
        }

        prenotazioneRepository.saveAll(prenotazioniList);
        logger.info(count + " prenotazioni per oggi, domani e dopodomani caricate");


    }


    private List<UserEntity> userEntityListConverter(String fileName, RoleEntity roleEntity) throws IOException {

        List<UserDTO> userList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/" + fileName), new TypeReference<List<UserDTO>>() {
        });

        return userList.stream().map(userDTO -> new UserEntity(userDTO, new HashSet<>(Arrays.asList(roleEntity)), passwordEncoder)).collect(Collectors.toList());
    }
}
