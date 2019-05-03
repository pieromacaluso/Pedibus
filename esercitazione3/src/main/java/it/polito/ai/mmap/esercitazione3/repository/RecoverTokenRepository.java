package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecoverTokenRepository extends MongoRepository<RecoverTokenEntity, Integer> {
    RecoverTokenEntity findByEmail(String email);
}
