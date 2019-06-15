package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.PrenotazioneDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "reservations")
public class PrenotazioneEntity {

    @Id
    private ObjectId id;
    private String cfChild;
    private Date data;
    private String idLinea;
    private Integer idFermata;
    private boolean verso;
    private boolean presoInCarico;
    private boolean arrivatoScuola;

    public PrenotazioneEntity(PrenotazioneDTO prenotazioneDTO) {
        cfChild =prenotazioneDTO.getCfChild();
        data=prenotazioneDTO.getData();
        verso=prenotazioneDTO.getVerso();
        idFermata = prenotazioneDTO.getIdFermata();
        idLinea = prenotazioneDTO.getIdLinea();
        this.presoInCarico = prenotazioneDTO.getPresoInCarico();
        this.arrivatoScuola = prenotazioneDTO.getArrivatoScuola();
    }

    public void update(PrenotazioneDTO prenotazioneDTO) {
        //non viene modificato l'id perchè si vuole solo aggiornare i campi della stessa prenotazione
        this.cfChild =prenotazioneDTO.getCfChild();
        this.data=prenotazioneDTO.getData();
        this.verso=prenotazioneDTO.getVerso();
        this.idFermata = prenotazioneDTO.getIdFermata();
        this.idLinea = prenotazioneDTO.getIdLinea();
        this.presoInCarico = prenotazioneDTO.getPresoInCarico();
        this.arrivatoScuola = prenotazioneDTO.getArrivatoScuola();
    }

}
