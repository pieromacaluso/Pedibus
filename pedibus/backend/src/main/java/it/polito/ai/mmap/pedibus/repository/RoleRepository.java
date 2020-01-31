package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface RoleRepository extends MongoRepository<RoleEntity, String> {
}
