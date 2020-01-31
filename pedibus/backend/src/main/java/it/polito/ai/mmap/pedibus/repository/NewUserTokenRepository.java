package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NewUserTokenEntity;
import it.polito.ai.mmap.pedibus.entity.RecoverTokenEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NewUserTokenRepository extends MongoRepository<NewUserTokenEntity, ObjectId> {
}
