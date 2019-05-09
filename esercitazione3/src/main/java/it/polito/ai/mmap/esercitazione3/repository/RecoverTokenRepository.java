package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecoverTokenRepository extends MongoRepository<RecoverTokenEntity, Integer> {
    Optional<RecoverTokenEntity> findByUsername(String email);
}
