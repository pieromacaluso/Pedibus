package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.FermataEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FermataRepository extends MongoRepository<FermataEntity, Integer> {
    Optional<FermataEntity> findById(Integer id);
}
