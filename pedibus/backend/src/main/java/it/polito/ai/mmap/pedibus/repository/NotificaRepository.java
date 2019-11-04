package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificaRepository extends MongoRepository<NotificaEntity, String> {
}
