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
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Environment environment;

    /**
     * crea:
     * - 100 Child
     * - 50 genitori con 2 figli        username = primi 50 contenuti nel file userDTO.json e pw = 1!qwerty1!
     * - 50 nonni                       username = secondi 50 contenuti nel file userDTO.json e pw = 1!qwerty1!
     * - 1 prenotazione/figlio per oggi, domani e dopo domani (o andata o ritorno)
     */
    public void makeChildUserPrenotazioni() throws IOException {
        prenotazioneRepository.deleteAll();
        int count = 0;

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
        RoleEntity roleAdmin = roleRepository.findByRole("ROLE_ADMIN");
        List<UserEntity> listNonni = new LinkedList<>();
        while (i < userList.size()) {
            UserEntity nonno = userList.get(i);
            Optional<UserEntity> checkDuplicate = userRepository.findByUsername(nonno.getUsername());
            if (!checkDuplicate.isPresent()) {
                nonno.setRoleList(new HashSet<>(Arrays.asList(roleAdmin)));
                nonno.setEnabled(true);
                listNonni.add(nonno);
                Optional<LineaEntity> check;
                switch (i % 3) {
                    case 0:
                        check = lineaRepository.findByNome("linea1");
                        break;
                    case 1:
                        check = lineaRepository.findByNome("linea2");
                        break;
                    default:
                        // Fake linea3 per inserire dei nonni non amministratori di linea
                        check = lineaRepository.findByNome("linea3");
                        break;
                }

                if (check.isPresent()) {
                    LineaEntity lineaEntity = check.get();
                    lineaEntity.getAdminList().add(nonno.getUsername());
                    lineaRepository.save(lineaEntity);
                }
                count++;
            }
            i++;
        }

        userRepository.saveAll(listNonni);
        logger.info(count + " nonni caricati");

        i = 0;
        count = 0;
        List<PrenotazioneEntity> prenotazioniList = new LinkedList<>();
        PrenotazioneEntity prenotazioneEntity;
        for (int day = 0; day < 3; day++) {
            childEntityIterable = childList.iterator();
            while (childEntityIterable.hasNext()) {
                ChildEntity childEntity = childEntityIterable.next();
                int randFermata = (Math.abs(new Random().nextInt()) % 8) + 1; //la linea 1 ha 8 fermate
                prenotazioneEntity = new PrenotazioneEntity();
                prenotazioneEntity.setCfChild(childEntity.getCodiceFiscale());
                prenotazioneEntity.setData(MongoZonedDateTime.parseData("yyyy-MM-dd HH:mm z", LocalDate.now().plus(day, ChronoUnit.DAYS).toString() + " 12:00 GMT+00:00"));
                prenotazioneEntity.setIdFermata(randFermata);
                prenotazioneEntity.setNomeLinea("linea1");
                prenotazioneEntity.setVerso(randFermata < 5); //1-4 = true = andata

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


    private List<UserEntity> userEntityListConverter() throws IOException {
        RoleEntity roleUser = roleRepository.findByRole("ROLE_USER");

        List<UserDTO> userList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/userDTO.json"), new TypeReference<List<UserDTO>>() {
        });

        return userList.stream().map(userDTO -> new UserEntity(userDTO, new HashSet<>(Arrays.asList(roleUser)), passwordEncoder)).collect(Collectors.toList());
    }
}
