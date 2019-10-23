package it.polito.ai.mmap.pedibus;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import it.polito.ai.mmap.pedibus.entity.*;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.DispAllResource;
import it.polito.ai.mmap.pedibus.services.LineeService;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
public class DocTest {
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

    Map<Integer, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    RoleEntity roleUser;
    RoleEntity roleAdmin;


    // Turno di default su cui lavoriamo, per poterlo riportare allo stato pre test
    int daysDef = 100;
    LineaEntity lineaDef;
    Boolean versoDef = true;
    Boolean isDefTurnoOpen;

    @PostConstruct
    public void postInit() {
        lineaDef = lineaRepository.findAll().get(0);
        Optional<TurnoEntity> checkTurno = turnoRepository.findByIdLineaAndDataAndVerso(lineaDef.getId(), MongoZonedDateTime.getMongoZonedDateTimeFromDate(LocalDate.now().plus(daysDef, ChronoUnit.DAYS).toString()), versoDef);
        if (checkTurno.isPresent())
            isDefTurnoOpen = checkTurno.get().getIsOpen();
        else
            isDefTurnoOpen = true;

        roleUser = roleRepository.findById("ROLE_USER").get();
        roleAdmin = roleRepository.findById("ROLE_ADMIN").get();
        childMap.put(0, new ChildEntity("RSSMRA30A01H501I", "Mario", "Rossi"));
        childMap.put(1, new ChildEntity("SNDPTN80C15H501C", "Sandro", "Pertini"));
        childMap.put(2, new ChildEntity("CLLCRL80A01H501D", "Carlo", "Collodi"));

        userDTOMap.put("testGenitore", new UserDTO("testGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonGenitore", new UserDTO("testNonGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonno", new UserDTO("testNonno@test.it", "321@%$User", "321@%$User"));

        userEntityMap.put("testGenitore", new UserEntity(userDTOMap.get("testGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder, new HashSet<>(Arrays.asList(childMap.get(0).getCodiceFiscale()))));
        userEntityMap.put("testNonGenitore", new UserEntity(userDTOMap.get("testNonGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder));
        userEntityMap.put("testNonno", new UserEntity(userDTOMap.get("testNonno"), new HashSet<>(Arrays.asList(roleAdmin)), passwordEncoder));

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
        TurnoEntity turno = turnoRepository.findByIdLineaAndDataAndVerso(lineaDef.getId(), MongoZonedDateTime.getMongoZonedDateTimeFromDate(LocalDate.now().plus(daysDef, ChronoUnit.DAYS).toString()), versoDef).get();
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
    public void getLines() throws Exception {

        String token = loginAsNonnoAdmin(lineaDef.getId());

        List<String> expectedResult = lineaRepository.findAll().stream().map(LineaEntity::getId).collect(Collectors.toList());
        String expectedJson = objectMapper.writeValueAsString(expectedResult);

        mockMvc.perform(get("/lines")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("get-lines",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void postDisp() throws Exception {

        String token = loginAsNonnoAdmin(lineaDef.getId());

        mockMvc.perform(post("/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, LocalDate.now().plus(daysDef, ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(lineaDef.getAndata().get(0)))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("post-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    public void deleteDisp() throws Exception {
        String token = loginAsNonnoAdmin(lineaDef.getId());

        postDisp();
        mockMvc.perform(delete("/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, LocalDate.now().plus(daysDef, ChronoUnit.DAYS))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("delete-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }


    @Test
    public void getTurnoDisp() throws Exception {

        String token = loginAsNonnoAdmin(lineaDef.getId());
        postDisp();
        getTurnoDispMethod(lineaDef, token);

    }

    @Test
    public void putTurno() throws Exception {
        String token = loginAsNonnoAdmin(lineaDef.getId());
        mockMvc.perform(put("/turno/state/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, LocalDate.now().plus(daysDef, ChronoUnit.DAYS))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(false)))
                .andExpect(status().isOk())
                .andDo(document("put-turno",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }


    @Test
    public void postTurnoDisp() throws Exception {
        String token = loginAsNonnoAdmin(lineaDef.getId());

        //crea una disponibilità
        postDisp();

        //chiudi il turno
        putTurno();

        //conferma la disponibilità
        List<DispAllResource> dispList = getTurnoDispMethod(lineaDef, token);
        dispList.forEach(dispAllResource -> dispAllResource.setIsConfirmed(true));

        mockMvc.perform(post("/turno/disp/{idLinea}/{verso}/{data}", lineaDef.getId(), versoDef, LocalDate.now().plus(daysDef, ChronoUnit.DAYS))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dispList)))
                .andExpect(status().isOk())
                .andDo(document("post-turno-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    public List<DispAllResource> getTurnoDispMethod(LineaEntity lineaEntity, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/turno/disp/{idLinea}/{verso}/{data}", lineaEntity.getId(), true, LocalDate.now().plus(daysDef, ChronoUnit.DAYS))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("get-turno-disp",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<DispAllResource>>() {
        });

    }


    private String loginAsNonnoAdmin(String idLinea) throws Exception {
        UserDTO user = userDTOMap.get("testNonno");

        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }


    private String loginAsSystemAdmin() throws Exception {

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

}