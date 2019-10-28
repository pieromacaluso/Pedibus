package it.polito.ai.mmap.pedibus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaAckDTO;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.NotificaResource;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    Map<String,ArrayList<NotificaBaseEntity>> notificheBaseEntityMap = new HashMap<>();        //Per ogni email(Utente), una lista delle sue notifiche Base
    Map<String,ArrayList<NotificaAckDTO>> notificheAckDTOMap= new HashMap<>();          //Per ogni email(Utente), una lista delle sue notifiche Ack
    RoleEntity roleUser;
    RoleEntity roleAdmin;
    RoleEntity roleGuide;

    LineaEntity lineaDef;

    @PostConstruct
    public void postInit() {
        //logger.info("PostInit init...");
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

        //logger.info("PostInit done");




    }

    @Before
    public void setUpMethod() {
        //logger.info("setUpMethod init...");
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
        //Notifiche Base
            //user1 //todo cxonvertire salvataggio mappa come per user
        notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg1",false));
        notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg2",true));
        notificaBaseRepository.save(new NotificaBaseEntity("testGenitore@test.it","msg3",false));
            //user2
        notificaBaseRepository.save(new NotificaBaseEntity("testNonGenitore@test.it","msg1",false));
        notificaBaseRepository.save(new NotificaBaseEntity("testNonGenitore@test.it","msg2",true));
        notificaBaseRepository.save(new NotificaBaseEntity("testNonGenitore@test.it","msg3",false));
            //user3
        notificaBaseRepository.save(new NotificaBaseEntity("testNonno@test.it","msg1",false));
        notificaBaseRepository.save(new NotificaBaseEntity("testNonno@test.it","msg2",true));
        notificaBaseRepository.save(new NotificaBaseEntity("testNonno@test.it","msg3",false));

        //logger.info("setUpMethod done.");
    }

    @After
    public void tearDownMethod() {
        //logger.info("tearDownMethod init...");
        userEntityMap.values().forEach(userEntity -> {
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.delAdminLine(userEntity.getUsername(), lineaDef.getId());
            List<NotificaBaseEntity> notificaBaseEntities=getAllNotificationBase(userEntity.getUsername());
            for(NotificaBaseEntity n:notificaBaseEntities){
                notificaBaseRepository.delete(n);
            }
            userRepository.delete(userEntity);
        });

        //logger.info("tearDownMethod done.");
    }

    private List<NotificaBaseEntity> getAllNotificationBase(String user) {
        return notificaBaseRepository.findAll().stream().filter(notificaBaseEntity -> notificaBaseEntity.getUsernameDestinatario().equals(user)).collect(Collectors.toList());
    }

    @Test
    public void deleteNotifica() throws Exception {
        logger.info("Test deleteNotifica Base...");
        String user="testGenitore@test.it";
        //autenticazione
        String token=loginAsGenitore();
        //ricerca idnotifica della notifica da eliminare, la prima non letta
        String idNotificaToDel=notificaBaseRepository.findAll().stream().filter(notificaBaseEntity -> notificaBaseEntity.getUsernameDestinatario().equals(user)).filter(notificaBaseEntity -> !notificaBaseEntity.getIsTouched()).map(NotificaBaseEntity::getIdNotifica).findFirst().get();

        //legge dal db tutte le notifiche non lette di quell utente e le mappa come notificaResource
        List<NotificaResource> expectedResult=notificaBaseRepository.findAll().stream().filter(notificaBaseEntity -> notificaBaseEntity.getUsernameDestinatario().equals(user)).filter(notificaBaseEntity -> !notificaBaseEntity.getIsTouched()).filter(notificaBaseEntity -> !notificaBaseEntity.getIdNotifica().equals(idNotificaToDel)).map(notificaBaseEntity -> {
            NotificaResource notificaResource=new NotificaResource(notificaBaseEntity.getIdNotifica(),notificaBaseEntity.getMsg());
            return notificaResource;
        }).collect(Collectors.toList());

        String expectedJson = objectMapper.writeValueAsString(expectedResult);

        mockMvc.perform(delete("/notifiche/{idNotifica}",idNotificaToDel)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifiche/{username}",user)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("delete-notifiche-base",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        logger.info("deleteNotifica Base done.");
        //todo test notificaAck
    }

    @Test
    public void getNotifiche() throws Exception {
        String user="testGenitore@test.it";
        logger.info("Test getNotifiche base user: "+user+" ...");
        //autenticazione
        String token=loginAsGenitore();
        //legge dal db tutte le notifiche non lette di quell utente e le mappa come notificaResource
        List<NotificaResource> expectedResult=notificaBaseRepository.findAll().stream().filter(notificaBaseEntity -> notificaBaseEntity.getUsernameDestinatario().equals(user)).filter(notificaBaseEntity -> !notificaBaseEntity.getIsTouched()).map(notificaBaseEntity -> {
                NotificaResource notificaResource=new NotificaResource(notificaBaseEntity.getIdNotifica(),notificaBaseEntity.getMsg());
                return notificaResource;
        }).collect(Collectors.toList());
        String expectedJson = objectMapper.writeValueAsString(expectedResult);

        mockMvc.perform(get("/notifiche/{username}",user)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("get-notifiche-base",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
        logger.info("test1 done.");
    }




    private String loginAsGenitore() throws Exception {
        UserDTO user = userDTOMap.get("testGenitore");

        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String loginAsNonnoAdmin(String idLinea) throws Exception {
        UserDTO user = userDTOMap.get("testNonno");

        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}
