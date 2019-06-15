package it.polito.ai.mmap.pedibus.repository;


import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LineaRepository extends MongoRepository<LineaEntity, Integer> {
     Optional<LineaEntity> findByNome(String name);
     Optional<LineaEntity> findById(String id);
}
