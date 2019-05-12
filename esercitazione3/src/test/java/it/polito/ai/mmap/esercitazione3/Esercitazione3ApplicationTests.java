package it.polito.ai.mmap.esercitazione3;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
import it.polito.ai.mmap.esercitazione3.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione3.services.MongoService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione3ApplicationTests {

    @Autowired
    JsonHandlerService jsonHandlerService;
    @Autowired
    MongoService mongoService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository userRepository;


    @Test
    public void postLogin_correct() throws Exception {
        logger.info("Test POST /login ...");
        UserDTO user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("mmapmmap1!");
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
        user.setPassword("mmapmmap1");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isUnauthorized());

        user.setEmail("applicazioni.internet.mmapgmail.com");
        user.setPassword("mmapmmap1");
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
        user.setEmail("pieromacaluso8@gmail.com");
        user.setPassword("123456789");
        user.setPassMatch("123456789");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("pieromacaluso8@gmail.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("pieromacaluso8@gmail.com").get());
        }
    }

    @Test
    public void postRegister_duplicate() throws Exception {
        logger.info("Test POST /register duplicate ...");
        UserDTO user = new UserDTO();
        user.setEmail("pieromacaluso8@gmail.com");
        user.setPassword("123456789");
        user.setPassMatch("123456789");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isOk());
        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("pieromacaluso8@gmail.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("pieromacaluso8@gmail.com").get());
        }
    }

    @Test
    public void postRegister_incorrect() throws Exception {
        logger.info("Test POST /register incorrect ...");
        UserDTO user = new UserDTO();
        logger.info("Passwords does not match ...");

        user.setEmail("pieromacaluso8@gmail.com");
        user.setPassword("123456789");
        user.setPassMatch("12345678");
        ObjectMapper mapper = new ObjectMapper();
        String json1 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json1))
                .andExpect(status().isInternalServerError());

        logger.info("Password not valid ...");

        user.setEmail("pieromacaluso8@gmail.com");
        user.setPassword("1");
        user.setPassMatch("1");
        String json2 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json2))
                .andExpect(status().isInternalServerError());

        logger.info("Email not valid ...");
        user.setEmail("pieromacaluso8gmail.com");
        user.setPassword("123456789");
        user.setPassMatch("123456789");
        String json3 = mapper.writeValueAsString(user);

        this.mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(json3))
                .andExpect(status().isInternalServerError());

        logger.info("PASSED");
        if (this.userRepository.findByUsername("pieromacaluso8@gmail.com").isPresent()) {
            this.userRepository.delete(this.userRepository.findByUsername("pieromacaluso8@gmail.com").get());
        }
    }

}