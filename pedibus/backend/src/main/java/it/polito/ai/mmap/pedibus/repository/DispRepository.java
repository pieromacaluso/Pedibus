package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DispRepository extends MongoRepository<DispEntity, ObjectId> {
    Optional<DispEntity> findByGuideUsernameAndTurnoId(String guideUsername, ObjectId turnoId);
    List<DispEntity> findAllByTurnoId(ObjectId turnoId);
}
