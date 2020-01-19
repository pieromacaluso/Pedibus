package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface ChildRepository extends MongoRepository<ChildEntity, String> {
    @Query(value = "{$or: [{'name': {$regex : ?0, $options: 'i'}}, {'surname': {$regex : ?0, $options: 'i'}}, {'codiceFiscale': {$regex : ?0, $options: 'i'}}]}", sort = "{'surname': 1, 'name': 1, '_id': 1}")
    Page<ChildEntity> searchByNameSurnameCF(String regex, Pageable pageable);
}
