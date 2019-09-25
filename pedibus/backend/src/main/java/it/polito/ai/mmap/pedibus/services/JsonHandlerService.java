package it.polito.ai.mmap.pedibus.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.exception.LineaNotFoundException;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
          try {
            Iterator<File> fileIterator = Arrays.asList(Objects.requireNonNull(ResourceUtils.getFile("classpath:lines//").listFiles())).iterator();
            while (fileIterator.hasNext()) {
                LineaDTO lineaDTO = objectMapper.readValue(fileIterator.next(), LineaDTO.class);
                try {
                    //Se ricarichiamo la linea con lo stesso nome ci ricopiamo gli admin
                    ArrayList<String> adminList = lineeService.getLineById(lineaDTO.getId()).getAdminList();
                    if (adminList != null)
                        lineaDTO.setAdminList(adminList);
                } catch (LineaNotFoundException e) {
                    lineaDTO.setAdminList(new ArrayList<>());
                }

                lineeService.addLinea(lineaDTO);
                lineeService.addFermate(lineaDTO.getAndata());
                lineeService.addFermate(lineaDTO.getRitorno());

                logger.info("Linea " + lineaDTO.getNome() + " caricata e salvata.");
            }

        } catch (IOException e) {
            logger.error("File riguardanti le linee mancanti");
            e.printStackTrace();
            System.exit(-1);
        }


    }
}