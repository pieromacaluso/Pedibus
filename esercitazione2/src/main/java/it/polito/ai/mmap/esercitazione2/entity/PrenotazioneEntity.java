package it.polito.ai.mmap.esercitazione2.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "reservations")
public class PrenotazioneEntity {
    @Id
    private CompositeKey id;

    private Integer idFermata;

    public PrenotazioneEntity(Integer idChild, Date data, boolean verso) {
        id.setIdChild(idChild);
        id.setData(data);
        id.setVerso(verso);
    }

    @Data
    static class CompositeKey {
        private Integer idChild;
        private Date data; //TODO è il formato migliore ?
        private boolean verso;
    }

}
