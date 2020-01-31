package it.polito.ai.mmap.pedibus.repository;


import it.polito.ai.mmap.pedibus.entity.LineaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.sound.sampled.LineEvent;
import java.util.List;
import java.util.Optional;

public interface LineaRepository extends MongoRepository<LineaEntity, String> {
    List<LineaEntity> findAllByAdminListContaining(String username);
    List<LineaEntity> findAllByMasterIs(String username);

}
