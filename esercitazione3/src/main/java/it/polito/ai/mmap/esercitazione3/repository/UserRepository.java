package it.polito.ai.mmap.esercitazione3.repository;

import it.polito.ai.mmap.esercitazione3.entity.RoleEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String email);

    Optional<UserEntity> findById(ObjectId id);

    Optional<UserEntity> findByUsernameAndIsEnabled(String email, Boolean isEnabled);

    Optional<UserEntity> findByRoleListContainingAndUsernameAndIsEnabled(RoleEntity roleEntity, String email, Boolean enabled);
}
