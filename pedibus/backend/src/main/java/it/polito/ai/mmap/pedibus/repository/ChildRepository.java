package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface ChildRepository extends MongoRepository<ChildEntity, String> {
    Page<ChildEntity> findAllByNameContainingOrSurnameContainingOrCodiceFiscaleContainingOrderBySurnameAscNameAscCodiceFiscaleAsc(String name, String surname, String codiceFiscale, Pageable pageable);

}
