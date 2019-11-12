package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificaRepository extends MongoRepository<NotificaEntity, String> {
    List<NotificaEntity> findAllByUsernameDestinatarioAndIsAckAndIsTouched(String user, boolean isAck, boolean isTouched);
}
