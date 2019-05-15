package it.polito.ai.mmap.esercitazione3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione3.model.Prenotazione;
import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import it.polito.ai.mmap.esercitazione3.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione3.services.MongoService;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione2ApplicationTests {

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
    public void getLines() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDTO();
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Piero").idFermata(1).verso(false).build();
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Piero").idFermata(1).verso(true).build();
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Piero").idFermata(1).verso(false).build();
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Marco").idFermata(1).verso(true).build();
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
                .andExpect(jsonPath("$.nomeAlunno").value(res.getNomeAlunno()))
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Marco").idFermata(1).verso(true).build();
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Marco").idFermata(1).verso(true).build();
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
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0]").value(res.getNomeAlunno()));
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Marco").idFermata(1).verso(true).build();
        String resJson = mapper.writeValueAsString(res);
        logger.info("Inserimento " + res + "...");

        MvcResult result1 = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(resJson)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        String idRes = result1.getResponse().getContentAsString();
        logger.info("Inserito correttamente!");


        logger.info("Modifico Prenotazione in modo errato");
        Prenotazione resWrong = Prenotazione.builder().nomeAlunno("Angelo").idFermata(5).verso(true).build();
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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
        String json = mapper.writeValueAsString(user);
        MvcResult result = this.mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("token").asText();

        Prenotazione res = Prenotazione.builder().nomeAlunno("Marco").idFermata(1).verso(true).build();
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
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0]").value(res.getNomeAlunno()));
        logger.info("Inserito e controllato correttamente!");


        logger.info("Modifico Prenotazione ...");
        Prenotazione resCorrect = Prenotazione.builder().nomeAlunno("Angelo").idFermata(5).verso(false).build();
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
                .andExpect(jsonPath("$.alunniPerFermataRitorno[0].alunni[0]").value(resCorrect.getNomeAlunno()));

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
        user.setEmail("applicazioni.internet.mmap@gmail.com");
        user.setPassword("12345@Sys");
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




}