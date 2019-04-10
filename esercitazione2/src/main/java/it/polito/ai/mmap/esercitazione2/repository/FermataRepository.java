package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FermataRepository extends MongoRepository<FermataEntity, Integer> {
}
