package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaAckEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificaAckRepository extends MongoRepository<NotificaAckEntity, String> {
}
