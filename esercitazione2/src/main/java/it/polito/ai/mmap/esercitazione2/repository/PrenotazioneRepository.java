package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PrenotazioneRepository extends MongoRepository<PrenotazioneEntity, Integer> {

    PrenotazioneEntity findById(ObjectId id);
    PrenotazioneEntity findByNomeAlunnoAndDataAndVerso(String nome,String data,boolean verso);
    List<String> findAllNomeAlunnoByDataAndIdFermataAndVerso(String data,Integer id,boolean verso);
}
