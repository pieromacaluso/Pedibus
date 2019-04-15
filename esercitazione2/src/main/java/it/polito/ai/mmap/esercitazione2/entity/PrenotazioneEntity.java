package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import lombok.Data;
import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Document(collection = "reservations")
public class PrenotazioneEntity {
    @Id
    private ObjectId id;
    //private CompositeKeyPrenotazione id;
    private String nomeAlunno;
    private String data;
    private boolean verso;
    private Integer idFermata;
    private Integer idLinea;

    // non eliminare
    public PrenotazioneEntity() {
    }

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        //id = new CompositeKeyPrenotazione(prenotazioneDTO.getNomeAlunno(), prenotazioneDTO.getData(), prenotazioneDTO.getVerso());
        nomeAlunno=prenotazioneDTO.getNomeAlunno();
        data=prenotazioneDTO.getData();
        verso=prenotazioneDTO.getVerso();
        idFermata = prenotazioneDTO.getIdFermata();
        idLinea = prenotazioneDTO.getLineaDTO().getId();
    }
}
