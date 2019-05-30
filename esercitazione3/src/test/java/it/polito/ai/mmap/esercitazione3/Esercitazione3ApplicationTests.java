package it.polito.ai.mmap.esercitazione3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.entity.ActivationTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.objectDTO.PermissionDTO;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.ActivationTokenRepository;
import it.polito.ai.mmap.esercitazione3.repository.RecoverTokenRepository;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import it.polito.ai.mmap.esercitazione3.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione3.services.LineeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione3ApplicationTests {

    @Autowired
    JsonHandlerService jsonHandlerService;
    @Autowired
    LineeService lineeService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecoverTokenRepository recoverTokenRepository;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;


    @Test
    public void postLogin_correct() throws Exception {
        logger.info("Test POST /login ...");
        UserDTO user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        logger.info("PASSED");
    }

    @Test
    public void postLogin_incorrect() throws Exception {
        logger.info("Test POST /login ...");
        UserDTO user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("aimaimaim");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isUnauthorized());

        user.setEmail("applicazioni.internet.mmapgmail.com");
        user.setPassword("aimaimaim");
        String json2 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isUnauthorized());

        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("1");
        String json3 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isUnauthorized());

        logger.info("PASSED");
    }

    @Test
    public void postRegister_correct() throws Exception {
        logger.info("Test POST /register ...");
        UserDTO user = new UserDTO();
        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("appmmap@pieromacaluso.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("appmmap@pieromacaluso.com").get());
        }
    }

    @Test
    public void postRegister_duplicate() throws Exception {
        logger.info("Test POST /register duplicate ...");
        UserDTO user = new UserDTO();
        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());
        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("appmmap@pieromacaluso.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("appmmap@pieromacaluso.com").get());
        }
    }

    @Test
    public void postRegister_incorrect() throws Exception {
        logger.info("Test POST /register incorrect ...");
        UserDTO user = new UserDTO();
        logger.info("Passwords does not match ...");

        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("12345678");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("Password not valid ...");

        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("1");
        user.setPassMatch("1");
        String json2 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isInternalServerError());

        logger.info("Email not valid ...");
        user.setEmail("appmmappieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        String json3 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("appmmap@pieromacaluso.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("appmmap@pieromacaluso.com").get());
        }
    }

    @Test
    public void getConfirmRandomUUID_correct() throws Exception {
        logger.info("Test GET /confirm/{randomUUID} correct ...");
        UserDTO user = new UserDTO();
        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> checkUser = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        assert checkUser.isPresent();
        Optional<ActivationTokenEntity> activationCheck = this.activationTokenRepository.findByUserId(checkUser.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        this.mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        logger.info("PASSED");
        this.userRepository.delete(checkUser.get());

    }

    @Test
    public void getConfirmRandomUUID_incorrect() throws Exception {
        logger.info("Test GET /confirm/{randomUUID} incorrect ...");

        this.mockMvc.perform(get("/confirm/123456789"))
                .andExpect(status().isNotFound());

        logger.info("PASSED");
    }

    @Test
    public void postRecover_always200() throws Exception {
        logger.info("Test ALL /recover");
        String email = "appmmap@pieromacaluso.com";
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> check = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = this.activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        this.mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isOk());

        Optional<RecoverTokenEntity> recoverCheck = this.recoverTokenRepository.findByUserId(check.get().getId());
        assert recoverCheck.isPresent();
        UUID = recoverCheck.get().getId().toString();
        this.mockMvc.perform(get("/recover/" + UUID))
                .andExpect(status().isOk());
        UserDTO user1 = new UserDTO();
        user1.setPassword("12345@User");
        user1.setPassMatch("12345@User");
        String json2 = mapper.writeValueAsString(user1);
        this.mockMvc.perform(post("/recover/" + UUID).contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isOk());

        user.setPassword("12345@User");
        String json3 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isOk());

        email = "ciao";
        this.mockMvc.perform(post("/recover").contentType(MediaType.APPLICATION_JSON).content(email))
                .andExpect(status().isOk());

        logger.info("PASSED");
        Optional<UserEntity> userCheck = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        if (userCheck.isPresent()) {
            Optional<RecoverTokenEntity> tokenCheck = this.recoverTokenRepository.findById(userCheck.get().getId());
            tokenCheck.ifPresent(recoverTokenEntity -> this.recoverTokenRepository.delete(recoverTokenEntity));
            this.userRepository.delete(userCheck.get());
        }
    }

    @Test
    public void getUserTest() throws Exception {
        logger.info("Test GET /users");
        // No user --> Unauthorized
        this.mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        // User with no rights --> Forbidden
        String email = "appmmap@pieromacaluso.com";
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        Optional<UserEntity> check = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = this.activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        this.mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());

        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        // No user --> Unauthorized
        this.mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // User authorized --> 200 OK
        user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);

        result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        node = mapper.readTree(result.getResponse().getContentAsString());
        token = node.get("token").asText();

        // No user --> Unauthorized
        this.mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        logger.info("PASSED");
        Optional<UserEntity> userCheck = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        if (userCheck.isPresent()) {
            Optional<RecoverTokenEntity> tokenCheck = this.recoverTokenRepository.findById(userCheck.get().getId());
            tokenCheck.ifPresent(recoverTokenEntity -> this.recoverTokenRepository.delete(recoverTokenEntity));
            this.userRepository.delete(userCheck.get());
        }
    }

    @Test
    public void putUsers() throws Exception {

        logger.info("Test PUT /users");
        // SYS-ADMIN
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        // USER-TEST
        String email = "appmmap@pieromacaluso.com";
        user = new UserDTO();
        user.setEmail(email);
        user.setPassword("321@User");
        user.setPassMatch("321@User");
        mapper = new ObjectMapper();
        json = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        Optional<UserEntity> check = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        assert check.isPresent();
        Optional<ActivationTokenEntity> activationCheck = this.activationTokenRepository.findByUserId(check.get().getId());
        assert activationCheck.isPresent();
        String UUID = activationCheck.get().getId().toString();
        this.mockMvc.perform(get("/confirm/" + UUID))
                .andExpect(status().isOk());
        PermissionDTO perm = new PermissionDTO();
        perm.setLinea("linea1");
        perm.setAddOrDel(true);
        json = mapper.writeValueAsString(perm);

        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        perm.setAddOrDel(false);
        json = mapper.writeValueAsString(perm);
        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());

        user.setEmail("appmmap@pieromacaluso.com");
        user.setPassword("321@User");
        json = mapper.writeValueAsString(user);
        result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        node = mapper.readTree(result.getResponse().getContentAsString());
        token = node.get("token").asText();

        perm.setAddOrDel(false);

        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        perm.setAddOrDel(true);

        this.mockMvc.perform(put("/users/" + "appmmap@pieromacaluso.com")
                .contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        logger.info("PASSED");
        Optional<UserEntity> userCheck = this.userRepository.findByUsername("appmmap@pieromacaluso.com");
        if (userCheck.isPresent()) {
            Optional<RecoverTokenEntity> tokenCheck = this.recoverTokenRepository.findById(userCheck.get().getId());
            tokenCheck.ifPresent(recoverTokenEntity -> this.recoverTokenRepository.delete(recoverTokenEntity));
            this.userRepository.delete(userCheck.get());
        }
    }


}
