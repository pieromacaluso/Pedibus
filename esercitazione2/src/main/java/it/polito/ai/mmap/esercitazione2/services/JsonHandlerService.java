package it.polito.ai.mmap.esercitazione2.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione2.objectDTO.Linea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class JsonHandlerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    ObjectMapper objectMapper;

    /**
     * Metodo che legge i JSON delle fermate e li salva sul DB
     *
     *
     * @return void
     */
    public void readPiedibusLines() {


        int countPiedibusLine = 0;
        try {
            countPiedibusLine = ResourceUtils.getFile("classpath:lines//").list().length;
            countPiedibusLine++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < countPiedibusLine; i++) {
            try {

                Linea linea = objectMapper.readValue(ResourceUtils.getFile("classpath:lines/line" + i +".json"), Linea.class);
                logger.info(linea.getNome());
                //TODO add linea to db


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
