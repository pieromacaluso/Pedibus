package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.repository.FermataRepository;
import it.polito.ai.mmap.esercitazione2.repository.ListaMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ListaMongoRepository listaMongoRepository;

    @Autowired
    private FermataRepository fermataRepository;


    /**
     * Salva una linea sul DB
     *
     * @param lineaDTO
     */
    public void addLinea(LineaDTO lineaDTO) {
        LineaEntity lineaEntity = new LineaEntity(lineaDTO);
        listaMongoRepository.save(lineaEntity);

    }

    /**
     * Salva fermate dul DB
     *
     * @param listaFermate
     */

    public void addFermate(ArrayList<FermataDTO> listaFermate) {

        fermataRepository.saveAll(listaFermate
                .stream()
                .map(FermataEntity::new)
                .collect(Collectors.toList()));
    }
}
