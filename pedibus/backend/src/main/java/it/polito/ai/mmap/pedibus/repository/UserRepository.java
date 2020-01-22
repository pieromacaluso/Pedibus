package it.polito.ai.mmap.pedibus.repository;

import it.polito.ai.mmap.pedibus.entity.RoleEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {
    Optional<UserEntity> findByUsername(String email);

    Optional<List<UserEntity>> findAllByChildrenListIsContaining(String child);

    Optional<UserEntity> findByUsernameAndIsEnabled(String email, Boolean isEnabled);

    Optional<List<UserEntity>> findAllByRoleListContainingOrderBySurnameAscNameAscUsernameAsc(RoleEntity roleEntity);
    Optional<List<UserEntity>> findAllByRoleListIn(List<RoleEntity> roleEntity);

    Optional<UserEntity> findByRoleListContainingAndUsernameAndIsEnabled(RoleEntity roleEntity, String email, Boolean enabled);

    @Query(
            value = "{$or: [{'name': {$regex : ?0, $options: 'i'}}, {'surname': {$regex : ?0, $options: 'i'}}, {'username': {$regex : ?0, $options: 'i'}}]}",
            sort = "{'surname': 1, 'name': 1, 'username': 1}"
    )
    Page<UserEntity> searchByNameSurnameCF(String fromKeywordToRegex, Pageable pageable);
}
