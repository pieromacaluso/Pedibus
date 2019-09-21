package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends MongoRepository<ReservationEntity, ObjectId> {
    Optional<ReservationEntity> findByCfChildAndDataAndVerso(String cfChild, Date data, boolean verso);
    Optional<ReservationEntity> findByCfChildAndData(String cfChild, Date data);
    List<ReservationEntity> findAllByDataAndIdFermataAndVerso(Date data, Integer id, boolean verso);
    List<ReservationEntity> deleteAllByCfChild(String cfChild);
    List<ReservationEntity> findByDataAndVerso(Date data,boolean verso);
}
