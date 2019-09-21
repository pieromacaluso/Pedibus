package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {
    Optional<UserEntity> findByUsername(String email);
    Optional<UserEntity> findByUsernameAndIsEnabled(String email, Boolean isEnabled);
    Optional<UserEntity> findByRoleListContainingAndUsernameAndIsEnabled(RoleEntity roleEntity, String email, Boolean enabled);
}
