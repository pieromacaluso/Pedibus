package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrenotazioneRepository extends MongoRepository<PrenotazioneEntity, Integer> {


}
