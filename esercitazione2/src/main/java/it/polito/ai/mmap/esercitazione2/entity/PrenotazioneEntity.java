package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import lombok.Data;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * TODO cancellare
 * source:
 * https://github.com/cybuch/sample-spring-data-mongo-composite-key/blob/master/src/main/java/ovh/cybuch/composite/key/StudentChairman.java
 */
@Data
@Document(collection = "reservations")
public class PrenotazioneEntity {
    @Id
    private CompositeKeyPrenotazione id;

    private Integer idFermata;
    private Integer idLinea;

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        id = new CompositeKeyPrenotazione(prenotazioneDTO.getNomeAlunno(), prenotazioneDTO.getData(), prenotazioneDTO.getVerso());
        idFermata = prenotazioneDTO.getIdFermata();
        idLinea = prenotazioneDTO.getLineaDTO().getId();
    }
}
