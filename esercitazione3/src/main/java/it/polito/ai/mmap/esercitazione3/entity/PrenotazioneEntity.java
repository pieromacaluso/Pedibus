package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.objectDTO.PrenotazioneDTO;
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
    private ObjectId idChild;
    private Date data;
    private boolean verso;
    private Integer idFermata;
    private String nomeLinea;
    private boolean presoInCarico;
    private boolean arrivatoScuola;

    // non eliminare
    public PrenotazioneEntity() {
    }

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        idChild=prenotazioneDTO.getIdChild();
        data=prenotazioneDTO.getData();
        verso=prenotazioneDTO.getVerso();
        idFermata = prenotazioneDTO.getIdFermata();
        nomeLinea = prenotazioneDTO.getNomeLinea();
    }

    public void update(PrenotazioneDTO prenotazioneDTO) {
        //non viene modificato l'id perchè si vuole solo aggiornare i campi della stessa prenotazione
        this.idChild=prenotazioneDTO.getIdChild();
        this.data=prenotazioneDTO.getData();
        this.verso=prenotazioneDTO.getVerso();
        this.idFermata = prenotazioneDTO.getIdFermata();
        this.nomeLinea = prenotazioneDTO.getNomeLinea();
    }

}
