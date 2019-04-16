package it.polito.ai.mmap.esercitazione2.repository;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import it.polito.ai.mmap.esercitazione2.entity.LineaEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface FermataRepository extends MongoRepository<FermataEntity, Integer> {
    Optional<FermataEntity> findById(String id);

    ArrayList<FermataEntity> findAllById(List<String> idList);

    List<FermataEntity> findAllByNomeLineaAndVerso(String nomeLinea, Boolean verso);

}
