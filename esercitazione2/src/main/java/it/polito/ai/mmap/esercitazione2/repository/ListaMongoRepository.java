package it.polito.ai.mmap.esercitazione2.repository;


import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ListaMongoRepository extends MongoRepository<LineaDTO, Integer> {
     LineaDTO findByName(String name);
}
