package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FermataRepository extends MongoRepository<FermataEntity, Integer> {
}
