package it.polito.ai.mmap.pedibus;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaAckDTO;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaBaseDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.services.LineeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NotificheTest {
    private Logger logger = LoggerFactory.getLogger(Esercitazione2ApplicationTests.class);

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    LineaRepository lineaRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ChildRepository childRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    DispRepository dispRepository;

    @Autowired
    TurnoRepository turnoRepository;

    @Autowired
    NotificaBaseRepository notificaBaseRepository;

    @Autowired
    LineeService lineeService;

    @Autowired
    PasswordEncoder passwordEncoder;

    Map<Integer, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    Map<String,ArrayList<NotificaBaseDTO>> notificheBaseDTOMap= new HashMap<>();        //Per ogni email(Utente), una lista delle sue notifiche Base
    Map<String,ArrayList<NotificaAckDTO>> notificheAckDTOMap= new HashMap<>();          //Per ogni email(Utente), una lista delle sue notifiche Ack
    RoleEntity roleUser;
    RoleEntity roleAdmin;
    RoleEntity roleGuide;

    LineaEntity lineaDef;

    @PostConstruct
    public void postInit() {
        lineaDef = lineaRepository.findAll().get(0);

        roleUser = roleRepository.findById("ROLE_USER").get();
        roleAdmin = roleRepository.findById("ROLE_ADMIN").get();
        roleGuide = roleRepository.findById("ROLE_GUIDE").get();

        childMap.put(0, new ChildEntity("RSSMRA30A01H501I", "Mario", "Rossi"));
        childMap.put(1, new ChildEntity("SNDPTN80C15H501C", "Sandro", "Pertini"));
        childMap.put(2, new ChildEntity("CLLCRL80A01H501D", "Carlo", "Collodi"));

        userDTOMap.put("testGenitore", new UserDTO("testGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonGenitore", new UserDTO("testNonGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonno", new UserDTO("testNonno@test.it", "321@%$User", "321@%$User"));

        userEntityMap.put("testGenitore", new UserEntity(userDTOMap.get("testGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder, new HashSet<>(Arrays.asList(childMap.get(0).getCodiceFiscale()))));
        userEntityMap.put("testNonGenitore", new UserEntity(userDTOMap.get("testNonGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder));
        userEntityMap.put("testNonno", new UserEntity(userDTOMap.get("testNonno"), new HashSet<>(Arrays.asList(roleAdmin, roleGuide)), passwordEncoder));

        logger.info("postInit finito.");
        /*ArrayList<NotificaBaseDTO> notificaBaseDTOs=new ArrayList<>();
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase1","testGenitore@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase2","testGenitore@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase3","testGenitore@test.it","msg",false));
        notificheBaseDTOMap.put("testGenitore@test.it",notificaBaseDTOs);

        notificaBaseDTOs.clear();
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase4","testNonGenitore@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase5","testNonGenitore@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase6","testNonGenitore@test.it","msg",false));
        notificheBaseDTOMap.put("testNonGenitore@test.it",notificaBaseDTOs);

        notificaBaseDTOs.clear();
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase7","testNonno@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase8","testNonno@test.it","msg",false));
        notificaBaseDTOs.add(new NotificaBaseDTO("notificaBase9","testNonno@test.it","msg",false));
        notificheBaseDTOMap.put("testNonno@test.it",notificaBaseDTOs);*/


    }

    @Before
    public void setUpMethod() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();

        logger.info("Il nonno sarà admin della linea: " + lineaDef.getId());

        userEntityMap.values().forEach(userEntity -> {
            userEntity.setEnabled(true);
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.addAdminLine(userEntity.getUsername(), lineaDef.getId());
            userRepository.save(userEntity);
        });
        //todo salvataggio diverse notifiche per ogni utente in userDTOMap
        //Notifiche Base
            //user1
        /*notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg1",false));
        notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg2",true));
        notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg3",false));*/

    }

    @After
    public void tearDownMethod() {
        userEntityMap.values().forEach(userEntity -> {
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.delAdminLine(userEntity.getUsername(), lineaDef.getId());
            userRepository.delete(userEntity);
        });
        //todo eliminare notifiche salvate precedentemente

    }

    @Test
    public void getNotifiche(){
        logger.info("test1...");
    }

    @Test
    public void deleteNotifica(){
        logger.info("test2...");
    }

}
