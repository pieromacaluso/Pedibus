package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PrenotazioneRepository extends MongoRepository<PrenotazioneEntity, Integer> {

    Optional<PrenotazioneEntity> findById(ObjectId id);
    Optional<PrenotazioneEntity> findByNomeAlunnoAndDataAndVerso(String nome, Date data, boolean verso);
    List<PrenotazioneEntity> findAllByDataAndIdFermataAndVerso(Date data, Integer id, boolean verso);
}
