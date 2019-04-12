package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
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

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        id = new CompositeKey();
        id.setNomeAlunno(prenotazioneDTO.getNomeAlunno());
        id.setData(prenotazioneDTO.getData());
        id.setVerso(prenotazioneDTO.getVerso());
        idFermata = prenotazioneDTO.getIdFermata();

    }

    @Data
    static class CompositeKey {
        private String nomeAlunno;
        private Date data;
        private boolean verso;
    }

}
