package it.polito.ai.mmap.esercitazione2.repository;


import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LineaRepository extends MongoRepository<LineaEntity, Integer> {
     LineaEntity findByNome(String name);
     Optional<LineaEntity> findById(Integer id);
}
