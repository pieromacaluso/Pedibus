package it.polito.ai.mmap.esercitazione2.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.NomeLineaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

                LineaDTO lineaDTO = objectMapper.readValue(ResourceUtils.getFile("classpath:lines/line" + i +".json"), LineaDTO.class);
                //logger.info(lineaDTO.toString());
                mongoService.addLinea(lineaDTO);
                mongoService.addFermate(lineaDTO.getAndata());
                mongoService.addFermate(lineaDTO.getRitorno());
                logger.info("Linea "+ lineaDTO.getId()+" caricata e salvata.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo che legge tutte le linee dal db e crea un json con solo i nomi di tale linee
     *
     * @return String
     * */
    public String getAllNameLines() {
        List<NomeLineaDTO> nameLines=new ArrayList<>();
        nameLines=mongoService.getAllLines().stream().map(NomeLineaDTO::new).collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(nameLines);
        }catch (JsonProcessingException e){
            return "lineNonDisponibili";            //todo check se necessario
        }

    }
}
