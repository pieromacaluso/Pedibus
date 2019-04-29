package it.polito.ai.mmap.esercitazione2.requirements;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.polito.ai.mmap.esercitazione2.bean.User;
import it.polito.ai.mmap.esercitazione2.controller.HomeController;
import it.polito.ai.mmap.esercitazione2.services.JsonHandlerService;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import net.minidev.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class Esercitazione2RequirementsTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JsonHandlerService jsonHandlerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    MongoService mongoService;


    @Before
    public void insertData() throws Exception {
        logger.info("Caricamento linee in corso...");
        jsonHandlerService.readPiedibusLines();
        logger.info("Caricamento linee completato.");
    }


    /**
     * GET /lines test
     *
     * @throws Exception
     */
    @Test
    public void getLines() throws Exception {
        this.mockMvc.perform(get("/lines")).andExpect(status().isOk())
                .andExpect(content().json("[\"linea1\",\"linea2\"]"));
    }

    /**
     * GET /lines/{nome_linea} test
     *
     * @throws Exception
     */
    @Test
    public void getLine() throws Exception {
        this.mockMvc.perform(get("/lines/linea1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("linea1"));
        this.mockMvc.perform(get("/lines/linea2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("linea2"));
    }

    /**
     * GET /reservations/* test
     *
     * @throws Exception
     */
    @Test
    public void reservationTest() throws Exception {
        JSONObject res1 = new JSONObject();
        res1.appendField("nomeAlunno", "Piero");
        res1.appendField("idFermata", 1);
        res1.appendField("verso", 0);

        logger.info("Provo a inserire reservation con verso errato");
        this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(res1.toString()))
                .andExpect(status().isInternalServerError());

        logger.info("Provo a inserire reservation con verso giusto");
        res1.replace("verso", 1);
        MvcResult res1Result = this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(res1.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String idRes1 = res1Result.getResponse().getContentAsString();

        logger.info("Controllo reservation " + idRes1);
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/" + idRes1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeAlunno").value("Piero"))
                .andExpect(jsonPath("$.idFermata").value(1))
                .andExpect(jsonPath("$.verso").value(true));

        logger.info("Controllo posizione nomeAlunno nelle linee di " + idRes1);
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataAndata[0].alunni[0]").value("Piero"));

        logger.info("Provo a inserire di nuovo la stessa, mi aspetto errore");
        this.mockMvc.perform(post("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON).content(res1.toString()))
                .andExpect(status().isInternalServerError());

        logger.info("Modifico Reservation in modo errato");
        res1.replace("nomeAlunno", "Angelo");
        res1.replace("idFermata", 5);
        res1.replace("verso", 1);
        this.mockMvc.perform(put("/reservations/linea1/2019-01-01/" + idRes1)
                .contentType(MediaType.APPLICATION_JSON).content(res1.toString()))
                .andExpect(status().isInternalServerError());

        logger.info("Modifico Reservation in modo corretto");
        res1.replace("verso", 0);
        this.mockMvc.perform(put("/reservations/linea1/2019-01-01/" + idRes1)
                .contentType(MediaType.APPLICATION_JSON).content(res1.toString()))
                .andExpect(status().isOk());

        logger.info("Controllo nuova posizione posizione nomeAlunno nelle linee di " + idRes1);
        this.mockMvc.perform(get("/reservations/linea1/2019-01-01/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunniPerFermataRitorno[0].alunni[0]").value("Angelo"));


    }

    @After
    public void deleteData() throws Exception {
//        mongoService.dropAll();
        logger.info("Reset linee in corso...");
        jsonHandlerService.readPiedibusLines();
        logger.info("Reset linee completato.");
    }
}