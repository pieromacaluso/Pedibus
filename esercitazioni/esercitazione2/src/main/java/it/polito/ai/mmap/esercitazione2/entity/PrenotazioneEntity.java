package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.PrenotazioneDTO;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "reservations")
public class PrenotazioneEntity {
    @Id
    private ObjectId id;
    private String nomeAlunno;
    private Date data;
    private boolean verso;
    private Integer idFermata;
    private Integer idLinea;

    // non eliminare
    public PrenotazioneEntity() {
    }

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        nomeAlunno=prenotazioneDTO.getNomeAlunno();
        data=prenotazioneDTO.getData();
        verso=prenotazioneDTO.getVerso();
        idFermata = prenotazioneDTO.getIdFermata();
        idLinea = prenotazioneDTO.getIdLinea();
    }

    public void update(PrenotazioneDTO prenotazioneDTO) {
        //non viene modificato l'id perchè si vuole solo aggiornare i campi della stessa prenotazione
        this.nomeAlunno=prenotazioneDTO.getNomeAlunno();
        this.data=prenotazioneDTO.getData();
        this.verso=prenotazioneDTO.getVerso();
        this.idFermata = prenotazioneDTO.getIdFermata();
        this.idLinea = prenotazioneDTO.getIdLinea();
    }

}
