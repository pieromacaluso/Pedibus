package it.polito.ai.mmap.pedibus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.entity.ActivationTokenEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import it.polito.ai.mmap.pedibus.entity.RecoverTokenEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.resources.PermissionResource;
import it.polito.ai.mmap.pedibus.objectDTO.UserDTO;
import it.polito.ai.mmap.pedibus.repository.ActivationTokenRepository;
import it.polito.ai.mmap.pedibus.repository.LineaRepository;
import it.polito.ai.mmap.pedibus.repository.RecoverTokenRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import it.polito.ai.mmap.pedibus.services.LineeService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * - Usando i dati di test qualsiasi aggiunta o modifica viene cancellata dopo ogni test
 * - Non supporre che tra 4, n giorni le uniche prenotazioni siano quelle introdotte come test
 * - usare solo gli endpoint http che si stanno testando (a meno che lo si faccia per comodità), per il resto fare accesso diretto al db
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione3ApplicationTests {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Value("${superadmin.password}")
    private String superAdminPass;

    @Autowired
    LineeService lineeService;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LineaRepository lineaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    private String mailTest = "appmmap@pieromacaluso.com";

    @After
    public void tearDownMethod() {
        Optional<UserEntity> check = userRepository.findByUsername(mailTest);
        if (check.isPresent()) {
            userRepository.delete(check.get());
            recoverTokenRepository.deleteByUserId(check.get().getId());
        }

        //cancello l'utente di test dalla lista degli admin di tutte le linee
        List<LineaEntity> lineaEntityList = lineaRepository.findAll();
        lineaEntityList.forEach(lineaEntity -> lineaEntity.getAdminList().remove(mailTest));
        lineaRepository.saveAll(lineaEntityList);
    }

    /**
     * Controlla l'endpoint POST /login con dati corretti
     *
     * @throws Exception
     */
    @Test
    public void postLogin_correct() throws Exception {
        logger.info("Test POST /login ...");

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla l'endpoint POST /login con username e pw sbagliata
     *
     * @throws Exception
     */
    @Test
    public void postLogin_incorrect() throws Exception {
        logger.info("Test POST /login ...");

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword("aimaimaim");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isUnauthorized());

        user.setEmail("applicazioni.internet.mmapgmail.com");
        user.setPassword("aimaimaim");
        String json2 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isUnauthorized());

        user.setEmail(superAdminMail);
        user.setPassword("1");
        String json3 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isUnauthorized());

        logger.info("PASSED");
    }

    /**
     * Controlla POST /register con dati corretti
     *
     * @throws Exception
     */
    @Test
    public void postRegister_correct() throws Exception {
        logger.info("Test POST /register ...");
        UserDTO user = new UserDTO();
        user.setEmail(mailTest);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla POST /register con un utente duplicato
     *
     * @throws Exception
     */
    @Test
    public void postRegister_duplicate() throws Exception {
        logger.info("Test POST /register duplicate ...");
        UserDTO user = new UserDTO();
        user.setEmail(mailTest);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());
        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
    }

    /**
     * Controlla POSt /register con pw che non matchano, email non valida, pw troppo corta
     *
     * @throws Exception
     */
    @Test
    public void postRegister_incorrect() throws Exception {
        logger.info("Test POST /register incorrect ...");
        UserDTO user = new UserDTO();
        logger.info("Passwords does not match ...");

        user.setEmail(mailTest);
        user.setPassword("321@User");
        user.setPassMatch("12345678");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("Password not valid ...");

        user.setEmail(mailTest);
        user.setPassword("1");
        user.setPassMatch("1");
        String json2 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isInternalServerError());

        logger.info("Email not valid ...");
        user.setEmail("appmmappieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        String json3 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
    }

    /**
     * Controlla GET /confirm/{randomUUID} con dati corretti
     *
     * @throws Exception
     */
    @Test
    public void getConfirmRandomUUID_correct() throws Exception {
        logger.info("Test GET /confirm/{randomUUID} correct ...");
        UserDTO user = new UserDTO();
        user.setEmail(mailTest);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> checkUser = userRepository.findByUsername(mailTest);
        assert checkUser.isPresent();
        Optional<ActivationTokenEntity> activationCheck = activationTokenRepository.findByUserId(checkUser.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla GET /confirm/{randomUUID} con un UUID non appartente a nessuno
     *
     * @throws Exception
     */
    @Test
    public void getConfirmRandomUUID_incorrect() throws Exception {
        logger.info("Test GET /confirm/{randomUUID} incorrect ...");

        mockMvc.perform(get("/confirm/123456789"))
                .andExpect(status().isNotFound());

        logger.info("PASSED");
    }

    /**
     * Controlla che POST /recover risponda sempre 200 come da specifiche
     *
     * @throws Exception
     */
    @Test
    public void postRecover_always200() throws Exception {
        logger.info("Test POST /recover");
        String email = mailTest;
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> check = userRepository.findByUsername(mailTest);
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isOk());

        Optional<RecoverTokenEntity> recoverCheck = recoverTokenRepository.findByUserId(check.get().getId());
        assert recoverCheck.isPresent();
        UUID = recoverCheck.get().getId().toString();
        UserDTO user1 = new UserDTO();
        user1.setPassword("12345@User");
        user1.setPassMatch("12345@User");
        String json2 = objectMapper.writeValueAsString(user1);
        mockMvc.perform(post("/recover/" + UUID).contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isOk());

        user.setPassword("12345@User");
        String json3 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isOk());

        email = "ciao";
        mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla che GET /admin/users sia accessibile solo all'admin
     *
     * @throws Exception
     */
    @Test
    public void getUserTest() throws Exception {
        logger.info("Test GET /admin/users");
        // No user --> Unauthorized
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());

        // User with no rights --> Forbidden
        String email = mailTest;
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        String json1 = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> check = userRepository.findByUsername(mailTest);
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        // No user --> Unauthorized
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // User authorized --> 200 OK
        user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);

        result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        node = objectMapper.readTree(result.getResponse().getContentAsString());
        token = node.get("token").asText();

        // No user --> Unauthorized
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    /**
     * Controlla PUT /admin/users/{userID}
     *
     * @throws Exception
     */
    @Test
    public void putUsers() throws Exception {

        logger.info("Test PUT /admin/users");
        // SYS-ADMIN

        UserDTO user = new UserDTO();
        user.setEmail(superAdminMail);
        user.setPassword(superAdminPass);
        String json = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        // USER-TEST
        String email = mailTest;
        user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");

        json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        Optional<UserEntity> check = userRepository.findByUsername(mailTest);
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        LineaEntity lineaEntity = lineaRepository.findAll().get(0);
        PermissionResource perm = new PermissionResource();
        perm.setIdLinea(lineaEntity.getId());
        perm.setAddOrDel(true);
        json = objectMapper.writeValueAsString(perm);

        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        assert lineaRepository.findById(lineaEntity.getId()).get().getAdminList().contains(mailTest);

        perm.setAddOrDel(false);
        json = objectMapper.writeValueAsString(perm);
        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        assert !lineaRepository.findById(lineaEntity.getId()).get().getAdminList().contains(mailTest);


        user.setEmail(mailTest);
        user.setPassword("321@User");
        json = objectMapper.writeValueAsString(user);
        result = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        node = objectMapper.readTree(result.getResponse().getContentAsString());
        token = node.get("token").asText();

        perm.setAddOrDel(false);
        json = objectMapper.writeValueAsString(perm);
        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        perm.setAddOrDel(true);
        json = objectMapper.writeValueAsString(perm);
        mockMvc.perform(put("/admin/users/" + mailTest)
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        logger.info("PASSED");

    }
}
