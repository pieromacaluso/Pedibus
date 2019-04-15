package it.polito.ai.mmap.esercitazione2.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//TODO nome della classe da rivedere, ma questi metodi non centravano con JsonHandlerService

@Service
public class LineService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MongoService mongoService;


    /**
     * Metodo che restituisce una lista di LineaDTO
     *
     * @return ArrayList<LineaDTO>
     */
    public ArrayList<LineaDTO> getAllLines() {
        ArrayList<LineaDTO> result = new ArrayList<>();
        result.addAll(mongoService.getAllLines().stream().map(lineaEntity -> new LineaDTO(lineaEntity, mongoService)).collect(Collectors.toList()));
        return result;
    }

    /**
     * Metodo che restituisce una lista di string contenente i nomi delle linee
     * @return List<String>
     */
    public List<String> getAllLinesName() {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(mongoService.getAllLines().stream().map(lineaEntity -> lineaEntity.getNome()).collect(Collectors.toList()));
        return result;
    }

    /**
     * Metodo che restituisce una LineaDTO a partire dal suo nome
     * @param lineName
     * @return LineaDTO
     */
    public LineaDTO getLine(String lineName) {
        LineaEntity lineaEntity = mongoService.getLine(lineName);
        return new LineaDTO(lineaEntity, mongoService);

    }
}
