package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.RecoverTokenEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RecoverTokenRepository extends MongoRepository<RecoverTokenEntity, ObjectId> {
    Optional<RecoverTokenEntity> findByUserId(ObjectId id);

}
