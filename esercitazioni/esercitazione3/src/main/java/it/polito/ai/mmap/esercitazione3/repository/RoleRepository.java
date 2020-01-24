package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface RoleRepository extends MongoRepository<RoleEntity,Integer> {
    RoleEntity findByRole(String role);
    RoleEntity findRoleEntityByRole(String role);

}
