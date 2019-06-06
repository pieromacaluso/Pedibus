package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ChildRepository extends MongoRepository<ChildEntity, String> {
}
