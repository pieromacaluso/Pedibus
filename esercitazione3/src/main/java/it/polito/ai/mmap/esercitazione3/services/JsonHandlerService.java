package it.polito.ai.mmap.esercitazione3.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione3.exception.LineaNotFoundException;
import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class JsonHandlerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${superadmin.email}")
    private String superAdminMail;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LineeService lineeService;


    /**
     * Metodo che legge i JSON delle fermate e li salva sul DB
     */
    public void readPiedibusLines() {
        int countPiedibusLine = 0;
        try {
            countPiedibusLine = Objects.requireNonNull(ResourceUtils.getFile("classpath:lines//").list()).length;
            countPiedibusLine++;
        } catch (IOException e) {
            logger.error("Directory lines inesistente");
            e.printStackTrace();
            System.exit(-1);
        }
        for (int i = 1; i < countPiedibusLine; i++) {
            try {
                LineaDTO lineaDTO = objectMapper.readValue(ResourceUtils.getFile("classpath:lines/line" + i + ".json"), LineaDTO.class);
                try {
                    //Se ricarichiamo la linea con lo stesso nome ci ricopiamo gli admin
                    ArrayList<String> adminList = lineeService.getLineByName(lineaDTO.getNome()).getAdminList();
                    if (adminList != null)
                        lineaDTO.setAdminList(adminList);
                } catch (LineaNotFoundException e) {

                    lineaDTO.setAdminList(new ArrayList<>());
                }

                lineeService.addLinea(lineaDTO);
                lineeService.addFermate(lineaDTO.getAndata());
                lineeService.addFermate(lineaDTO.getRitorno());

                logger.info("Linea " + lineaDTO.getNome() + " caricata e salvata.");
            } catch (IOException e) {
                logger.error("File lineN.json mancanti");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
