package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaBaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificaBaseRepository extends MongoRepository<NotificaBaseEntity, String> {
}
