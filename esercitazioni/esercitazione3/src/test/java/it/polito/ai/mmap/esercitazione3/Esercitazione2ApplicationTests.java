package it.polito.ai.mmap.esercitazione3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.entity.ChildEntity;
import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.ChildRepository;
import it.polito.ai.mmap.esercitazione3.repository.PrenotazioneRepository;
import it.polito.ai.mmap.esercitazione3.repository.RoleRepository;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import it.polito.ai.mmap.esercitazione3.resources.PrenotazioneResource;
import it.polito.ai.mmap.esercitazione3.services.*;
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
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



/*

        //Per creare velocemente 3 prenotazioni per 2019-01-01, linea1, fermata 1, andata
        childMap.values().forEach(childEntity -> {
            PrenotazioneEntity prenotazioneEntity = new PrenotazioneEntity();
            prenotazioneEntity.setCfChild(childEntity.getCodiceFiscale());
            prenotazioneEntity.setData(new Date());
            prenotazioneEntity.setIdFermata(1);
            prenotazioneEntity.setNomeLinea("linea1");
            prenotazioneEntity.setVerso(true);
            prenotazioneRepository.save(prenotazioneEntity);
        });
        System.exit(0);
*/

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
    PrenotazioneRepository prenotazioneRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    Map<String, ChildEntity> childMap = new HashMap<>();
    Map<String, UserDTO> userDTOMap = new HashMap<>();
    Map<String, UserEntity> userEntityMap = new HashMap<>();
    RoleEntity roleUser;
    RoleEntity roleAdmin;

    List<PrenotazioneEntity> prenotazioniList = new LinkedList<>();

    @PostConstruct
    public void postInit() {
        roleUser = roleRepository.findByRole("ROLE_USER");
        roleAdmin = roleRepository.findByRole("ROLE_ADMIN");
        childMap.put("Rossi", new ChildEntity("RSSMRA30A01H501I", "Mario", "Rossi", false));
        childMap.put("Pertini", new ChildEntity("SNDPTN80C15H501C", "Sandro", "Pertini", false));
        childMap.put("Collodi", new ChildEntity("CLLCRL80A01H501D", "Carlo", "Collodi", false));

        userDTOMap.put("testGenitore", new UserDTO("testGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonGenitore", new UserDTO("testNonGenitore@test.it", "321@%$User", "321@%$User"));
        userDTOMap.put("testNonno", new UserDTO("testNonno@test.it", "321@%$User", "321@%$User"));

        userEntityMap.put("testGenitore", new UserEntity(userDTOMap.get("testGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder, new HashSet<>(Arrays.asList(childMap.get("Rossi").getCodiceFiscale()))));
        userEntityMap.put("testNonGenitore", new UserEntity(userDTOMap.get("testNonGenitore"), new HashSet<>(Arrays.asList(roleUser)), passwordEncoder));
        userEntityMap.put("testNonno", new UserEntity(userDTOMap.get("testNonno"), new HashSet<>(Arrays.asList(roleAdmin)), passwordEncoder));

    }

    @Before
    public void setUpMethod() {
        //todo controllare se esiste già per non scrivere tante copie
        childRepository.saveAll(childMap.values());
        //todo controllare se esiste già per non scrivere tante copie
        userEntityMap.values().forEach(userEntity -> {
            userEntity.setEnabled(true);
            if (userEntity.getRoleList().contains(roleAdmin))
                lineeService.addAdminLine(userEntity.getUsername(), "linea1");
            userRepository.save(userEntity);
        });
        //todo penso siano anche da cancellare alla fine dei test per non sporcare il db
    }

    @After
    public void tearDownMethod() {
//        childMap.values().forEach(childEntity ->
//        {
//            prenotazioneRepository.deleteAllByCfChild(childEntity.getCodiceFiscale());
//            childRepository.delete(childEntity);
//        });
//        userEntityMap.values().forEach(userEntity -> {
//            if (userEntity.getRoleList().contains(roleAdmin))
//                lineeService.delAdminLine(userEntity.getUsername(), "linea1");
//            userRepository.delete(userEntity);
//        });
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
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add("linea1");
        expectedResult.add("linea2");
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

        String linea = "linea1";
        logger.info("Test GET /lines/" + linea + " ...");

        this.mockMvc.perform(get("/lines/" + linea)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(linea));

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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(false).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento errato " + res + "...");
        logger.info("POST /reservations/linea1/2019-01-01/ con verso errato ...");

        this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento corretto " + res + "...");
        logger.info("POST /reservations/linea1/2019-01-01/ con verso corretto ...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        String idRes = result1.getResponse().getContentAsString();
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("RSSMRA30A01H501I").idFermata(1).verso(false).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento errato " + res + "...");
        logger.info("POST /reservations/linea3/2019-01-01/ con linea errata ...");

        this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);

        logger.info("Inserimento " + res + "...");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String idRes = result1.getResponse().getContentAsString();
        logger.info("Inserito correttamente!");

        logger.info("Controllo reservation " + idRes + " ...");
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cfChild").value(res.getCfChild()))
                .andExpect(jsonPath("$.idFermata").value(res.getIdFermata()))
                .andExpect(jsonPath("$.verso").value(res.getVerso()));
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = result1.getResponse().getContentAsString();
        logger.info("Inserito correttamente!");

        logger.info("POST /reservations/linea1/2019-01-01/ duplicato ...");
        this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = result1.getResponse().getContentAsString();
        logger.info("Inserito correttamente!");

        logger.info("Controllo posizione nomeAlunno nelle linee di " + idRes);
        logger.info("GET /reservations/linea1/2019-01-01/ per controllo presenza utente ...");
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0].codiceFiscale").value(res.getCfChild()));
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = result1.getResponse().getContentAsString();
        logger.info("Inserito correttamente!");


        logger.info("Modifico Prenotazione in modo errato");
        PrenotazioneResource resWrong = PrenotazioneResource.builder().cfChild("CLLCRL80A01H501D").idFermata(5).verso(true).build();
        String resWrongJson = mapper.writeValueAsString(resWrong);

        this.mockMvc.perform(put("/reservations/linea1/2019-01-01/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resWrongJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("PASSED");

        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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

        PrenotazioneResource res = PrenotazioneResource.builder().cfChild("SNDPTN80C15H501C").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento e controllo posizione " + res + "...");
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = result1.getResponse().getContentAsString();

        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0].codiceFiscale").value(res.getCfChild()));
        logger.info("Inserito e controllato correttamente!");


        logger.info("Modifico Prenotazione ...");
        PrenotazioneResource resCorrect = PrenotazioneResource.builder().cfChild("CLLCRL80A01H501D").idFermata(5).verso(false).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);

        this.mockMvc.perform(put("/reservations/linea1/2019-01-01/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("Controllo nuova posizione prenotazione");
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataRitorno[0].alunni[0].codiceFiscale").value(resCorrect.getCfChild()));

        logger.info("PASSED");
        logger.info("Ripristino stato precedente...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/" + idRes)
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
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        logger.info("Cancellazione a caso errata con objectID...");
        this.mockMvc.perform(delete("/reservations/linea1/2019-01-01/5cc9c667c947dc1d2eb496ee")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
        logger.info("DONE");
    }

    @Test
    public void reservation_checkPermissionGenitore() throws Exception {
        logger.info("Test inserimento prenotazione per un proprio figlio");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        String idRes = inserimentoPrenotazioneGenitore();

        logger.info("Test modifica prenotazione per un proprio figlio");
        PrenotazioneResource resCorrect = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(2).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        MvcResult result1 = this.mockMvc.perform(put("/reservations/linea1/2019-02-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();


        logger.info("Test delete prenotazione per un proprio figlio");
        this.mockMvc.perform(delete("/reservations/linea1/2019-02-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


    }

    @Test
    public void reservation_checkPermissionNonGenitore() throws Exception {
        //creazione prenotazione valida
        String idRes = inserimentoPrenotazioneGenitore();

        logger.info("Test inserimento prenotazione per un figlio altrui");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testNonGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        PrenotazioneResource res = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento prenotazione: " + res);
        this.mockMvc.perform(post("/reservations/linea1/2019-03-02/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError()).andReturn();

        logger.info("Test modifica prenotazione per un figlio altrui");
        PrenotazioneResource resCorrect = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        this.mockMvc.perform(put("/reservations/linea1/2019-02-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());

        logger.info("Test delete prenotazione per un figlio altrui");
        this.mockMvc.perform(delete("/reservations/linea1/2019-02-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());


    }

    @Test
    public void reservation_checkPermissionNonno() throws Exception {
        logger.info("Test inserimento prenotazione Nonno");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testNonno"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        PrenotazioneResource res = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento prenotazione: " + res);
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-03-02/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        String idRes = result1.getResponse().getContentAsString();

        logger.info("Test modifica prenotazione per nonno");
        PrenotazioneResource resCorrect = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resCorrectJson = mapper.writeValueAsString(resCorrect);
        this.mockMvc.perform(put("/reservations/linea1/2019-04-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON).content(resCorrectJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("Test delete prenotazione per nonno");
        this.mockMvc.perform(delete("/reservations/linea1/2019-04-02/" + idRes)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

    }


    private String inserimentoPrenotazioneGenitore() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(userDTOMap.get("testGenitore"));
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        String token = mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        PrenotazioneResource res = PrenotazioneResource.builder().cfChild(userEntityMap.get("testGenitore").getChildrenList().iterator().next()).idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento prenotazione: " + res);
        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-02-02/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return result1.getResponse().getContentAsString();
    }
}
