package it.polito.ai.mmap.pedibus.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.exception.LineaNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
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
    ChildService childService;
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
    MongoTimeService mongoTimeService;
    @Autowired
    NotificheService notificheService;

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
        HashSet<RoleEntity> adminRoles = new HashSet<>();
        adminRoles.add(userService.getRoleEntityById("ROLE_SYSTEM-ADMIN"));

        UserEntity userEntity = new UserEntity(userDTO, adminRoles, passwordEncoder);
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
            Iterator<File> fileIterator = Arrays.asList(Objects.requireNonNull(ResourceUtils.getFile("classpath:building_data/lines//").listFiles())).iterator();
            while (fileIterator.hasNext()) {
                LineaDTO lineaDTO = objectMapper.readValue(fileIterator.next(), LineaDTO.class);
                try {
                    //Se ricarichiamo la linea con lo stesso nome ci ricopiamo gli admin
                    Set<String> adminList = lineeService.getLineaEntityById(lineaDTO.getId()).getAdminList();
                    if (adminList != null)
                        lineaDTO.setAdminList(adminList);

//                    ArrayList<String> guideList = lineeService.getLineaEntityById(lineaDTO.getId()).getGuideList();
//                    if (guideList != null)
//                        lineaDTO.setGuideList(guideList);

                } catch (LineaNotFoundException e) {
                    lineaDTO.setAdminList(new TreeSet<>());
//                    lineaDTO.setGuideList(new ArrayList<>());
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
     * Crea:
     * - countCreate genitori, ognuno con due figli
     * - countCreate guide
     * - 1 admin della linea 1
     * - 1 admin della linea 2
     * @param countCreate
     * @throws IOException
     */
    public void makeChildUser(int countCreate) throws IOException {
        RoleEntity roleUser = userService.getRoleEntityById("ROLE_USER");
        RoleEntity roleAdmin = userService.getRoleEntityById("ROLE_ADMIN");
        RoleEntity roleGuide = userService.getRoleEntityById("ROLE_GUIDE");


        List<LineaEntity> lineaEntityList = lineaRepository.findAll();


        List<ChildEntity> children = objectMapper.readValue(ResourceUtils.getFile("classpath:building_data/people/childEntity.json"), new TypeReference<List<ChildEntity>>() {
        });


        Iterator<UserEntity> userList = userEntityListConverter("genitori.json", roleUser).iterator();
        Iterator<ChildEntity> childList = children.iterator();
        int count = 0;
        while (count < countCreate) {
            UserEntity parent = userList.next();
            while (userRepository.findByUsername(parent.getUsername()).isPresent()) {
                if (!userList.hasNext())
                    return;
                parent = userList.next();
            }

            ChildEntity child1 = childList.next();
            while (childRepository.findById(child1.getCodiceFiscale()).isPresent()) {
                if (!childList.hasNext())
                    return;
                child1 = childList.next();
            }

            ChildEntity child2 = childList.next();
            while (childRepository.findById(child2.getCodiceFiscale()).isPresent()) {
                if (!childList.hasNext())
                    return;
                child2 = childList.next();
            }

            child1.setSurname(parent.getSurname());
            child2.setSurname(parent.getSurname());
            parent.setChildrenList(new HashSet<>(Arrays.asList(child1.getCodiceFiscale(), child2.getCodiceFiscale())));
            parent.setEnabled(true);

            userRepository.save(parent);
            childService.createChild(new ChildDTO(child1));
            childService.createChild(new ChildDTO(child2));

            count++;
        }
        logger.info(count + " genitori caricati, con due figli ognuno.");

        LinkedList<UserEntity> listNonni = new LinkedList<>();
        for (int i = 0; i <= 1; i++) {
            count = 0;
            Iterator<UserEntity> nonniList = userEntityListConverter("nonni_" + i + ".json", roleGuide).iterator();
            while (count < countCreate + 1) {
                UserEntity nonno = nonniList.next();
                while (userRepository.findByUsername(nonno.getUsername()).isPresent()) {
                    if (!userList.hasNext())
                        return;
                    nonno = nonniList.next();
                }
                nonno.setEnabled(true);

                if (count < 1) {
                    LineaEntity lineaEntity = lineaEntityList.get(i);
                    nonno.getRoleList().add(roleAdmin);
                    nonno.getRoleList().remove(roleGuide);

                    if (!lineaEntity.getAdminList().contains(nonno.getUsername()))
                        lineaEntity.getAdminList().add(nonno.getUsername());
                    lineaRepository.save(lineaEntity);
                }
                listNonni.add(nonno);

                count++;
            }
        }

        userRepository.saveAll(listNonni);
        logger.info(count * 2 + " nonni caricati");
    }


    private List<UserEntity> userEntityListConverter(String fileName, RoleEntity roleEntity) throws IOException {

        List<UserDTO> userList = objectMapper.readValue(ResourceUtils.getFile("classpath:building_data/people/" + fileName), new TypeReference<List<UserDTO>>() {
        });

        return userList.stream().map(userDTO -> new UserEntity(userDTO, new HashSet<>(Arrays.asList(roleEntity)), passwordEncoder)).collect(Collectors.toList());
    }

    private List<String> getCfList() throws IOException {
        List<String> res = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new FileReader(
                ResourceUtils.getFile("classpath:building_data/people/cf.txt")));
        String line = reader.readLine();
        while (line != null) {
            res.add(line);
            line = reader.readLine();
        }
        reader.close();
        return res;
    }

    public List<ChildEntity> transform() throws IOException {
        List<ChildEntity> childList = objectMapper.readValue(ResourceUtils.getFile("classpath:building_data/people/childEntity_base.json"), new TypeReference<List<ChildEntity>>() {
        });
        Iterator<String> cfList = getCfList().iterator();

        childList.forEach(childEntity -> {
            childEntity.setCodiceFiscale(cfList.next());
            System.out.println(childEntity);
        });

        return childList;
    }
}
