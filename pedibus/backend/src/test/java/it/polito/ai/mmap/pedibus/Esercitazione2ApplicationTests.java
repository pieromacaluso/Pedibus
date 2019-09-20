package it.polito.ai.mmap.pedibus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.*;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import it.polito.ai.mmap.pedibus.services.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)

@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione2ApplicationTests {

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Autowired
    JsonHandlerService jsonHandlerService;
    @Autowired
    LineeService lineeService;
    @Autowired
    UserService userService;

    private Logger logger = LoggerFactory.getLogger(Esercitazione2ApplicationTests.class);
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ChildRepository childRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LineaRepository lineaRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    Map<String, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    RoleEntity roleUser;
    RoleEntity roleAdmin;


    @PostConstruct
    public void postInit() {
        roleUser = roleRepository.findByRole("ROLE_USER");
        roleAdmin = roleRepository.findByRole("ROLE_ADMIN");
        childMap.put("Rossi", new ChildEntity("RSSMRA30A01H501I", "Mario", "Rossi"));
        childMap.put("Pertini", new ChildEntity("SNDPTN80C15H501C", "Sandro", "Pertini"));
        childMap.put("Collodi", new ChildEntity("CLLCRL80A01H501D", "Carlo", "Collodi"));

        userDTOMap.put("testGenitore", new UserDTO("testGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonGenitore", new UserDTO("testNonGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonno", new UserDTO("testNonno@test.it", "321@%$User", "321@%$User"));

        userEntityMap.put("testGenitore", new UserEntity(userDTOMap.get("testGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder, new HashSet<>(Arrays.asList(childMap.get("Rossi").getCodiceFiscale()))));
        userEntityMap.put("testNonGenitore", new UserEntity(userDTOMap.get("testNonGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder));
        userEntityMap.put("testNonno", new UserEntity(userDTOMap.get("testNonno"), new HashSet<>(Arrays.asList(roleAdmin)), passwordEncoder));

    }

    @Before
    public void setUpMethod() {
        childRepository.saveAll(childMap.values());
        userEntityMap.values().forEach(userEntity -> {
            userEntity.setEnabled(true);
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.addAdminLine(userEntity.getUsername(), "linea1");
            userRepository.save(userEntity);
        });
    }

    @After
    public void tearDownMethod() {
        childMap.values().forEach(childEntity ->
        {
            reservationRepository.deleteAllByCfChild(childEntity.getCodiceFiscale());
            childRepository.delete(childEntity);
        });
        userEntityMap.values().forEach(userEntity -> {
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.delAdminLine(userEntity.getUsername(), "linea1");
            userRepository.delete(userEntity);
        });
    }

    @Test
    public void getLines() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();
        logger.info("Test GET /lines ...");
        List<String> expectedResult = lineaRepository.findAll().stream().map(LineaEntity::getId).collect(Collectors.toList());
        String expectedJson = mapper.writeValueAsString(expectedResult);


        this.mockMvc.perform(get("/lines")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        logger.info("PASSED");
    }

    @Test
    public void getLine() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        String lineaID = lineaRepository.findAll().get(0).getId();
        logger.info("Test GET /lines/" + lineaID + " ...");

        this.mockMvc.perform(get("/lines/" + lineaID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lineaID));

        logger.info("PASSED");
    }

    @Test
    public void insertReservation_wrongVerso() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(false).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento errato " + res + "...");
        logger.info("POST /reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/ con verso errato ...");

        this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");

    }

    @Test
    public void insertReservation_correctVerso() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento corretto " + res + "...");
        logger.info("POST /reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/ con verso corretto ...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");

    }

    @Test
    public void insertReservation_wrongLine() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(false).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento errato " + res + "...");
        logger.info("POST /reservations/linea3/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/ con linea errata ...");

        this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");

    }

    @Test
    public void getReservation_check() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento " + res + "...");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("Controllo reservation " + idRes + " ...");
        this.mockMvc.perform(get("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cfChild").value(res.getCfChild()))
                .andExpect(jsonPath("$.idFermata").value(res.getIdFermata()))
                .andExpect(jsonPath("$.verso").value(res.getVerso()));
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");

    }

    /**
     * Test che controlla il funzionamento dell'engPoint GET /reservations/verso/{nome_linea}/{data}/{verso} con verso andata(true).
     * Tale endPoint deve ritornare tutte le reservations per una determinata combinazione di linea,data e verso.
     * La fase di test inizia senza nessuna reservation.
     * Vengono effettuate due reservations una con verso andata e una con verso ritorno.
     * Il test controlla che alla chiamata dell'endPoint con verso andata si ottenga solo la reservation di andata e non anche quella di ritorno.
     *
     * @throws Exception
     */
    @Test
    public void getReservationsTowardTrue_check() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource resTrue = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        ReservationResource resFalse = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(8).verso(false).build();
        String resTrueJson = mapper.writeValueAsString(resTrue);
        String resFalseJson = mapper.writeValueAsString(resFalse);

        logger.info("Inserimento " + resTrue + " with toward true and " + resFalse + " with toward false");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resTrueJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idResTrue = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        MvcResult result1False = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resFalseJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idResFalse = mapper.readValue(result1False.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("Controllo reservation with toward true ...");
        this.mockMvc.perform(get("/reservations/verso/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermata").isNotEmpty());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idResTrue)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idResFalse)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");

    }

    /**
     * Test che controlla il funzionamento dell'engPoint GET /reservations/verso/{nome_linea}/{data}/{verso} con verso ritorno(false).
     * Tale endPoint deve ritornare tutte le reservations per una determinata combinazione di linea,data e verso.
     * La fase di test inizia senza nessuna reservation.
     * Vengono effettuate due reservations una con verso andata e una con verso ritorno.
     * Il test controlla che alla chiamata dell'endPoint con verso ritorno si ottenga solo la reservation di ritorno e non anche quella di andata.
     *
     * @throws Exception
     */
    @Test
    public void getReservationsTowardFalse_check() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource resTrue = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        ReservationResource resFalse = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(8).verso(false).build();
        String resTrueJson = mapper.writeValueAsString(resTrue);
        String resFalseJson = mapper.writeValueAsString(resFalse);

        logger.info("Inserimento " + resTrue + " with toward true and " + resFalse + " with toward false");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resTrueJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idResTrue = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        MvcResult result1False = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resFalseJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idResFalse = mapper.readValue(result1False.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("Controllo reservation with toward true ...");
        this.mockMvc.perform(get("/reservations/verso/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermata").isNotEmpty());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idResTrue)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idResFalse)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");

    }

    /**
     * Test che controlla il funzionamento dell'endoPoint GET /notreservations/{data}/{verso}.
     * Tale endPoint deve ritornare solo i bambini che non hanno una reservation per tale data e verso.
     * La fase di test inizia senza nessuna reservation, si leggono quindi tutti i bambini.
     * Viene effettuata solo una reservation per una data e verso andata.
     * In tale caso, l' endPoint testato con verso ritorno dovrebbe ritornare tutti i bambini del db visto che non sono presenti reservations con verso ritorno.
     * Se però si usa l'endpoint con verso andata, si deve ottenere la lista dei bambini precedentemente letta a meno del bambino che ha effettuato la reservation con verso andata.
     *
     * @throws Exception
     */
    @Test
    public void getNotReservations_check() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);

        String json = mapper.writeValueAsString(user);

        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resTrueJson = mapper.writeValueAsString(res);


        logger.info("Inserimento reservation " + res + " andata ...");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resTrueJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("Lettura di tutti i bambini iscritti...");
        MvcResult result1Child = this.mockMvc.perform(get("/admin/children/")
                .contentType(MediaType.APPLICATION_JSON).content(resTrueJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idResChildren = result1Child.getResponse().getContentAsString();

        Map<String, List<Map<String, Object>>> allChildren = mapper.readValue(idResChildren, new TypeReference<Map<String, List<Map<String, Object>>>>() {
        });
        List<Map<String, Object>> listAllChildren = allChildren.get("ListaChildren");

        logger.info("Lettura eseguita!");

        logger.info("Controllo bambini non prenotati ritorno...");
        this.mockMvc.perform(get("/notreservations/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childrenNotReserved").value(listAllChildren));
        logger.info("Corretto");

        logger.info("Controllo bambini non prenotati andata...");

        //rimozione da allChildren del bambino che ha effettuato sopra la reservation
        Iterator<Map<String, Object>> i = listAllChildren.iterator();
        while (i.hasNext()) {
            Map<String, Object> child = i.next();
            if (child.containsKey("codiceFiscale")) {
                if (child.get("codiceFiscale").toString().compareTo("SNDPTN80C15H501C") == 0)
                    i.remove();
            }
        }

        this.mockMvc.perform(get("/notreservations/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childrenNotReserved").value(listAllChildren));
        logger.info("Corretto");


        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("DONE");

    }

    @Test
    public void insertReservation_duplicate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("POST /reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/ duplicato ...");
        this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");
    }

    @Test
    public void getReservation_checkReservationPositionInLine() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");

        logger.info("Controllo posizione nomeAlunno nelle linee di " + idRes);
        logger.info("GET /reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/ per controllo presenza utente ...");
        this.mockMvc.perform(get("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0].codiceFiscale").value(res.getCfChild()));
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");
    }

    @Test
    public void putReservation_updateWrong() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        logger.info("Inserito correttamente!");


        logger.info("Modifico Reservation in modo errato");
        ReservationResource resWrong = ReservationResource.builder().cfChild("CLLCRL80A01H501D").idFermata(5).verso(true).build();
        String resWrongJson = mapper.writeValueAsString(resWrong);

        this.mockMvc.perform(put("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resWrongJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");
    }

    @Test
    public void putReservation_updateCorrect_checkPosition() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        ReservationResource res = ReservationResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento e controllo posizione " + res + "...");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);


        this.mockMvc.perform(get("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0].codiceFiscale").value(res.getCfChild()));
        logger.info("Inserito e controllato correttamente!");


        logger.info("Modifico Reservation ...");
        ReservationResource resCorrect = ReservationResource.builder().cfChild("CLLCRL80A01H501D").idFermata(5).verso(false).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);

        this.mockMvc.perform(put("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("Controllo nuova posizione reservation");
        this.mockMvc.perform(get("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataRitorno[0].alunni[0].codiceFiscale").value(resCorrect.getCfChild()));

        logger.info("PASSED");
        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        logger.info("DONE");
    }

    @Test
    public void deleteReservation_randomID() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        logger.info("Cancellazione a caso errata con numero...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        logger.info("Cancellazione a caso errata con objectID...");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(4,ChronoUnit.DAYS) + "/5cc9c667c947dc1d2eb496ee")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("DONE");
    }

    @Test
    public void reservation_checkPermissionGenitore() throws Exception {
        logger.info("Test inserimento reservation per un proprio figlio");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        String idRes = inserimentoReservationGenitore();

        logger.info("Test modifica reservation per un proprio figlio");
        ReservationResource resCorrect = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(2).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        MvcResult result1 = this.mockMvc.perform(put("/reservations/linea1/" + LocalDate.now().plus(1, ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();


        logger.info("Test delete reservation per un proprio figlio");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(1, ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


    }

    @Test
    public void reservation_checkPermissionNonGenitore() throws Exception {
        //creazione reservation valida
        String idRes = inserimentoReservationGenitore();

        logger.info("Test inserimento reservation per un figlio altrui");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testNonGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        ReservationResource res = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento reservation: " + res);
        this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(2, ChronoUnit.DAYS) + "/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError()).andReturn();

        logger.info("Test modifica reservation per un figlio altrui");
        ReservationResource resCorrect = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        this.mockMvc.perform(put("/reservations/linea1/" + LocalDate.now().plus(1, ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());

        logger.info("Test delete reservation per un figlio altrui");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now().plus(1, ChronoUnit.DAYS) + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());


    }

    @Test
    public void reservation_checkPermissionNonno() throws Exception {
        logger.info("Test inserimento reservation Nonno");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testNonno"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        ReservationResource res = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento reservation: " + res);
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now())
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        String idRes = mapper.readValue(result1.getResponse().getContentAsString(), String.class);

        //Ora come ora un nonno con ruolo admin può prenotare solo per il giorno stesso

        logger.info("Test modifica reservation per nonno");
        ReservationResource resCorrect = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        this.mockMvc.perform(put("/reservations/linea1/" + LocalDate.now() + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("Test delete reservation per nonno");
        this.mockMvc.perform(delete("/reservations/linea1/" + LocalDate.now() + "/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

    }


    private String inserimentoReservationGenitore() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        ReservationResource res = ReservationResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento reservation: " + res);
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/" + LocalDate.now().plus(1, ChronoUnit.DAYS) + "/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return mapper.readValue(result1.getResponse().getContentAsString(), String.class);

    }
}
