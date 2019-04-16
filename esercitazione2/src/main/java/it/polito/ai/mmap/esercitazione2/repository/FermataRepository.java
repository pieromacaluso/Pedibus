package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FermataRepository extends MongoRepository<FermataEntity, Integer> {
    Optional<FermataEntity> findById(Integer id);

}
