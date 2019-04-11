package it.polito.ai.mmap.esercitazione2.repository;


import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LineaMongoRepository extends MongoRepository<LineaEntity, Integer> {
     LineaEntity findByNome(String name);
}
