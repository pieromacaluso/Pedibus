package it.polito.ai.mmap.pedibus;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.resources.TurnoDispResource;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Le Operazioni fatte devono avvenire sul turno di default per essere in grado di ripristinare lo stato precedente
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GestioneCorseTest {
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
    LineeService lineeService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MongoTimeService mongoTimeService;

    Map<Integer, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    RoleEntity roleUser;
    RoleEntity roleAdmin;
    RoleEntity roleGuide;


    // Turno di default su cui lavoriamo, per poterlo riportare allo stato pre test
    String daysDef;
    LineaEntity lineaDef;
    Boolean versoDef = true;
    Boolean isDefTurnoOpen;

    @PostConstruct
    public void postInit() {
        // necessario per trovare un turno che sia valido (vacanza,weekend ecc)
        daysDef = mongoTimeService.getOneValidDate(0);
        lineaDef = lineaRepository.findAll().get(0);
        Optional<TurnoEntity> checkTurno = turnoRepository.findByIdLineaAndDataAndVerso(lineaDef.getId(), mongoTimeService.getMongoZonedDateTimeFromDate(daysDef), versoDef);
        if (checkTurno.isPresent())
            isDefTurnoOpen = checkTurno.get().getIsOpen();
        else
            isDefTurnoOpen = true;

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
    }

    @After
    public void tearDownMethod() {
        TurnoEntity turno = turnoRepository.findByIdLineaAndDataAndVerso(lineaDef.getId(), mongoTimeService.getMongoZonedDateTimeFromDate(daysDef), versoDef).get();
        turno.setIsOpen(isDefTurnoOpen);
        turnoRepository.save(turno);

        userEntityMap.values().forEach(userEntity -> {
            dispRepository.deleteAllByGuideUsername(userEntity.getUsername());
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.delAdminLine(userEntity.getUsername(), lineaDef.getId());
            userRepository.delete(userEntity);
        });
    }

    @Test
    public void getDisp() throws Exception {

        String token = loginAsNonnoGuideAdmin(lineaDef.getId());
        postDisp();

        mockMvc.perform(get("/disp/{verso}/{data}", versoDef, daysDef)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("get-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void postDisp() throws Exception {

        String token = loginAsNonnoGuideAdmin(lineaDef.getId());

        mockMvc.perform(post("/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(lineaDef.getAndata().get(0)))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("post-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void deleteDisp() throws Exception {
        String token = loginAsNonnoGuideAdmin(lineaDef.getId());

        postDisp();
        mockMvc.perform(delete("/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("delete-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }




    @Test
    public void getTurno() throws Exception {
        String token = loginAsNonnoGuideAdmin(lineaDef.getId());

        mockMvc.perform(get("/turno/state/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("get-turno",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void putTurno() throws Exception {
        String token = loginAsNonnoGuideAdmin(lineaDef.getId());
        mockMvc.perform(put("/turno/state/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(false)))
                .andExpect(status().isOk())
                .andDo(document("put-turno",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void getTurnoDisp() throws Exception {

        String token = loginAsNonnoGuideAdmin(lineaDef.getId());
        postDisp();
        getTurnoDispMethod(lineaDef, token);

    }

    @Test
    public void postTurnoDisp() throws Exception {
        String token = loginAsNonnoGuideAdmin(lineaDef.getId());

        //crea una disponibilità
        postDisp();

        //chiudi il turno
        putTurno();

        //conferma la disponibilità
        TurnoDispResource turnoDispResource = getTurnoDispMethod(lineaDef, token);
        DispAllResource dispAllResource = turnoDispResource.getListDisp().values().stream().flatMap(List::stream).collect(Collectors.toList()).get(0);
        dispAllResource.setIsConfirmed(true);

        mockMvc.perform(post("/turno/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dispAllResource)))
                .andExpect(status().isOk())
                .andDo(document("post-turno-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void postDispAck() throws Exception {
        String token = loginAsNonnoGuideAdmin(lineaDef.getId());

        //crea e conferma la disponibilità
        postTurnoDisp();

        mockMvc.perform(post("/turno/disp/ack/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, daysDef)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("post-disp-ack",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    public TurnoDispResource getTurnoDispMethod(LineaEntity lineaEntity, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/turno/disp/{idLinea}/{verso}/{data}", lineaEntity.getId(), true, daysDef)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("get-turno-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TurnoDispResource.class);

    }


    private String loginAsNonnoGuideAdmin(String idLinea) throws Exception {
        UserDTO user = userDTOMap.get("testNonno");

        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}
