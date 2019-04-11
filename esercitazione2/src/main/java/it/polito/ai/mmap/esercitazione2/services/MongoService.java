package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.LineaMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LineaMongoRepository lineaMongoRepository;

    @Autowired
    private FermataRepository fermataRepository;


    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        lineaMongoRepository.save(lineaEntity);

    }

    /**
     * Salva fermate dul DB
     *
     * @param listaFermate
     */

    public void addFermate(List<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate
                .stream()
                .map(FermataEntity::new)
                .collect(Collectors.toList()));
    }

    /**
     * Salva fermate dul DB
     *
     */
    public List<LineaEntity> getAllLines() {
        return lineaMongoRepository.findAll();
    }

    public LineaEntity getLine(String lineName) {
        return lineaMongoRepository.findByNome(lineName);       //ToDo check unica linea con tale nome, forse farlo in fase di caricamento db
    }

    /**
     * Legge da db tutte le fermate presenti nella lista idFermata
     * @param idFermate
     * @return ordinato per orario
     */
    public List<FermataEntity> getFermate(List<Integer> idFermate) {
        List<FermataEntity> fermate=(List<FermataEntity>) fermataRepository.findAllById(idFermate);
        fermate.sort(Comparator.comparing(FermataEntity::getOrario));                                  //ordinate per orario e non per id
        return fermate;
    }
}
