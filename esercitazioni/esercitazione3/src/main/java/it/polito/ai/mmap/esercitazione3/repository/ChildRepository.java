package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.ChildEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ChildRepository extends MongoRepository<ChildEntity, String> {
}
