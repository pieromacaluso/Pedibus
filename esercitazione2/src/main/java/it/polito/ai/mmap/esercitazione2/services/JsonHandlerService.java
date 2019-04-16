package it.polito.ai.mmap.esercitazione2.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class JsonHandlerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MongoService mongoService;

    /**
     * Metodo che legge i JSON delle fermate e li salva sul DB
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
        mongoService.removeFermate(); //TODO al momento le cancello e riscrivo ogni volta perchè non sovrascrive
        for (int i = 1; i < countPiedibusLine; i++) {
            try {

                LineaDTO lineaDTO = objectMapper.readValue(ResourceUtils.getFile("classpath:lines/line" + i + ".json"), LineaDTO.class);
                // aggiorno la linea solo se non aggiornata //TODO per debug
                //if (!mongoService.LineaUpdated(lineaDTO) || mongoService.LineaUpdated(lineaDTO)) {
                lineaDTO.getAndata().stream().forEach(fermataDTO -> {
                    fermataDTO.setVerso(true); //TODO andata è true vero ?
                    fermataDTO.setNomeLinea(lineaDTO.getNome());
                });
                lineaDTO.getRitorno().stream().forEach(fermataDTO -> {
                    fermataDTO.setVerso(false); //TODO andata è true vero ?
                    fermataDTO.setNomeLinea(lineaDTO.getNome());
                });

                mongoService.addFermate(lineaDTO.getAndata());
                mongoService.addFermate(lineaDTO.getRitorno());
                mongoService.addLinea(lineaDTO);
                logger.info("Linea " + lineaDTO.getId() + " caricata e salvata.");
                //} else
                //    logger.info("Linea " + lineaDTO.getId() + "gia' aggiornata.");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
