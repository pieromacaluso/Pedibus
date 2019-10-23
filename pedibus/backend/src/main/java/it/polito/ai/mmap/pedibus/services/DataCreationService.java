package it.polito.ai.mmap.pedibus.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.LineaNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataCreationService {
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
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    FermataRepository fermataRepository;
    @Autowired
    LineeService lineeService;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private Environment environment;

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;


    /**
     * Metodo eseguito all'avvio della classe come init per leggere le linee del pedibus.
     */
    @PostConstruct
    public void init() throws IOException {
        logger.info("Caricamento ruoli e sysAdmin in corso...");
        createRolesAndSysAdmin();
        logger.info("Ruoli e sysAdmin gestiti correttamente.");

        logger.info("Caricamento linee in corso...");
        readPiedibusLines();
        logger.info("Caricamento linee completato.");
//        makeChildUserReservations();

        if (environment.getActiveProfiles()[0].equals("prod")) {
            logger.info("Creazione Basi di dati di test in corso...");
            makeChildUserReservations();
            logger.info("Creazione Basi di dati di test completata.");
        } else {
            logger.info("Creazione Basi di dati di test non effettuata con DEV Profile");
        }
    }

    /**
     * Crea i ruoli e se non presente, l'utente con privilegio SYSTEM-ADMIN. Tale utente è già abilitato senza l'invio dell'email.
     */
    public void createRolesAndSysAdmin() {
        ArrayList<String> roles = new ArrayList<>();

        roles.add("ROLE_USER");
        roles.add("ROLE_GUIDE");
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_SYSTEM-ADMIN");
        for (String id : roles) {
            Optional<RoleEntity> checkRole = roleRepository.findById(id);
            if (!checkRole.isPresent())
                roleRepository.save(new RoleEntity(id));
        }


        Optional<UserEntity> check = userRepository.findByUsername(superAdminMail);
        if (check.isPresent()) {
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(superAdminMail);
        userDTO.setPassword(superAdminPass);

        RoleEntity role = userService.getRoleEntityById("ROLE_SYSTEM-ADMIN");

        UserEntity userEntity = new UserEntity(userDTO, new HashSet<>(Arrays.asList(role)), passwordEncoder);
        userEntity.setEnabled(true);
        userRepository.save(userEntity);

// TODO dovrebbe essere una rimanenza di qualcosa che non serve più (marcof)

//        check = userRepository.findByUsername(superAdminMail);      //rileggo per poter leggere l'objectId e salvarlo come string
//        if (check.isPresent()) {
//            userEntity = check.get();
//            userRepository.save(userEntity);
//            logger.info("SuperAdmin configurato ed abilitato.");
//        }
    }


    /**
     * Metodo che legge i JSON delle fermate e li salva sul DB
     */
    public void readPiedibusLines() {
        try {
            Iterator<File> fileIterator = Arrays.asList(Objects.requireNonNull(ResourceUtils.getFile("classpath:lines//").listFiles())).iterator();
            while (fileIterator.hasNext()) {
                LineaDTO lineaDTO = objectMapper.readValue(fileIterator.next(), LineaDTO.class);
                try {
                    //Se ricarichiamo la linea con lo stesso nome ci ricopiamo gli admin
                    ArrayList<String> adminList = lineeService.getLineaEntityById(lineaDTO.getId()).getAdminList();
                    if (adminList != null)
                        lineaDTO.setAdminList(adminList);

                    ArrayList<String> guideList = lineeService.getLineaEntityById(lineaDTO.getId()).getGuideList();
                    if (guideList != null)
                        lineaDTO.setGuideList(guideList);

                } catch (LineaNotFoundException e) {
                    lineaDTO.setAdminList(new ArrayList<>());
                    lineaDTO.setGuideList(new ArrayList<>());
                }

                LineaEntity lineaEntity = new LineaEntity(lineaDTO);
                lineaRepository.save(lineaEntity);
                fermataRepository.saveAll(lineaDTO.getAndata().stream().map(fermataDTO -> new FermataEntity(fermataDTO, lineaEntity.getId())).collect(Collectors.toList()));
                fermataRepository.saveAll(lineaDTO.getRitorno().stream().map(fermataDTO -> new FermataEntity(fermataDTO, lineaEntity.getId())).collect(Collectors.toList()));

                logger.info("Linea " + lineaDTO.getNome() + " caricata e salvata.");
            }

        } catch (IOException e) {
            logger.error("File riguardanti le linee mancanti");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * crea:
     * - 100 Child
     * - 50 genitori con 2 figli        contenuti nel file genitori.json e pw = 1!qwerty1!
     * - 25 nonni GUIDE della prima linea    contenuti nel file nonni_0.json e pw = 1!qwerty1! i primi 5 sono anche admin
     * - 25 nonni GUIDE della seconda linea    contenuti nel file nonni_1.json e pw = 1!qwerty1! i primi 5 sono anche admin
     * - 1 reservation/figlio per oggi, domani e dopo domani (andata e ritorno)
     */
    public void makeChildUserReservations() throws IOException {
        RoleEntity roleUser = userService.getRoleEntityById("ROLE_USER");
        RoleEntity roleAdmin = userService.getRoleEntityById("ROLE_ADMIN");
        RoleEntity roleGuide = userService.getRoleEntityById("ROLE_GUIDE");
        reservationRepository.deleteAll();
        userRepository.deleteAll(userRepository.findAll().stream().filter(userEntity -> userEntity.getRoleList().contains(roleAdmin)).collect(Collectors.toList()));
        childRepository.deleteAll();
        int count = 0;

        List<LineaEntity> lineaEntityList = lineaRepository.findAll();


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
            parent.setEnabled(true);

            parent = userRepository.save(parent); //per avere l'objectId

            userList.set(i, parent);
            child1.setIdParent(parent.getId());
            child2.setIdParent(parent.getId());

            i++;
        }
        childRepository.saveAll(childList);
        logger.info("Tutti i genitori e i figli caricati");

        LinkedList<UserEntity> listNonni = new LinkedList<>();
        for (i = 0; i <= 1; i++) {
            count = 0;
            for (UserEntity nonno : userEntityListConverter("nonni_" + i + ".json", roleGuide)) {
                nonno.setEnabled(true);

                if (count < 5) {
                    LineaEntity lineaEntity = lineaEntityList.get(i);
                    nonno.getRoleList().add(roleAdmin);
                    if (!lineaEntity.getAdminList().contains(nonno.getUsername()))
                        lineaEntity.getAdminList().add(nonno.getUsername());
                    lineaRepository.save(lineaEntity);
                } else if (count < 30){
                    LineaEntity lineaEntity = lineaEntityList.get(i);
                    nonno.getRoleList().add(roleGuide);
                    if (!lineaEntity.getGuideList().contains(nonno.getUsername()))
                        lineaEntity.getGuideList().add(nonno.getUsername());
                    lineaRepository.save(lineaEntity);
                }
                listNonni.add(nonno);

                count++;
            }
        }

        userRepository.saveAll(listNonni);
        logger.info(count * i + " nonni caricati");

        i = 0;
        count = 0;
        List<ReservationEntity> reservationsList = new LinkedList<>();
        ReservationEntity reservationEntity;
        int randLinea;
        for (int day = 0; day < 3; day++) {
            childEntityIterable = childList.iterator();
            while (childEntityIterable.hasNext()) {
                ChildEntity childEntity = childEntityIterable.next();

                //andata
                reservationEntity = new ReservationEntity();
                reservationEntity.setCfChild(childEntity.getCodiceFiscale());
                reservationEntity.setData(MongoZonedDateTime.getMongoZonedDateTimeFromDate(LocalDate.now().plus(day, ChronoUnit.DAYS).toString()));
                reservationEntity.setIdLinea(lineeService.getFermataEntityById(childEntity.getIdFermataAndata()).getIdLinea());
                reservationEntity.setVerso(true);
                reservationEntity.setIdFermata(childEntity.getIdFermataAndata());
                if (!reservationRepository.findByCfChildAndData(reservationEntity.getCfChild(), reservationEntity.getData()).isPresent()) {
                    reservationsList.add(reservationEntity);
                    count++;
                }

                //ritorno
                reservationEntity = new ReservationEntity();
                reservationEntity.setCfChild(childEntity.getCodiceFiscale());
                reservationEntity.setData(MongoZonedDateTime.getMongoZonedDateTimeFromDate(LocalDate.now().plus(day, ChronoUnit.DAYS).toString()));
                reservationEntity.setIdLinea(lineeService.getFermataEntityById(childEntity.getIdFermataRitorno()).getIdLinea());
                reservationEntity.setVerso(false);
                reservationEntity.setIdFermata(childEntity.getIdFermataRitorno());

                if (!reservationRepository.findByCfChildAndData(reservationEntity.getCfChild(), reservationEntity.getData()).isPresent()) {
                    reservationsList.add(reservationEntity);
                    count++;
                }
                i++;
            }
        }

        reservationRepository.saveAll(reservationsList);
        logger.info(count + " reservations per oggi, domani e dopodomani caricate");
    }


    private List<UserEntity> userEntityListConverter(String fileName, RoleEntity roleEntity) throws IOException {

        List<UserDTO> userList = objectMapper.readValue(ResourceUtils.getFile("classpath:debug_container/" + fileName), new TypeReference<List<UserDTO>>() {
        });

        return userList.stream().map(userDTO -> new UserEntity(userDTO, new HashSet<>(Arrays.asList(roleEntity)), passwordEncoder)).collect(Collectors.toList());
    }
}
