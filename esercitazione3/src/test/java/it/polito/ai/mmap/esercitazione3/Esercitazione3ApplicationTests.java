package it.polito.ai.mmap.esercitazione3;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.model.Prenotazione;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione3.services.MongoService;
import it.polito.ai.mmap.esercitazione3.services.MongoZonedDateTime;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

}