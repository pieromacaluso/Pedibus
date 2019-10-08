package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.Optional;

public interface TurnoRepository extends MongoRepository<TurnoEntity, ObjectId> {
    Optional<TurnoEntity> findByIdLineaAndDataAndVerso(String idLinea, Date data, Boolean verso);
}
