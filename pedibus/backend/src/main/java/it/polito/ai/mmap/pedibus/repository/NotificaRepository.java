package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface NotificaRepository extends MongoRepository<NotificaEntity, String> {
    void deleteAllByUsernameDestinatario(String username);
    Optional<NotificaEntity> findByDispID(ObjectId dispID);
    void deleteByDispID(ObjectId dispID);
    Page<NotificaEntity> findAllByUsernameDestinatarioOrderByDataDesc(String user, Pageable pageable);
    List<NotificaEntity> findAllByUsernameDestinatarioAndIsAckAndIsTouchedAndAndType(String user, boolean isAck, boolean isTouched, NotificaEntity.NotificationType type);
}
