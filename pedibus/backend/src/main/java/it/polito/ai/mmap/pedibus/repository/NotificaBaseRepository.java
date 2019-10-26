package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaBaseEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificaBaseRepository extends MongoRepository<NotificaBaseEntity, ObjectId> {
}
