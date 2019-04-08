package it.polito.ai.mmap.esercitazione2.services;

import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.repository.ListaMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MongoService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ListaMongoRepository listaMongoRepository;


    /**
     * Salve una linea sul DB
     * @param lineaDTO
     */
    public void addLineToMongo(LineaDTO lineaDTO)
    {
        listaMongoRepository.save(lineaDTO);

    }
}
