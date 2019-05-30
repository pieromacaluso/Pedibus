package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.resources.PrenotazioneResource;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
public class PrenotazioneDTO {
    ObjectId idChild;
    Date data;
    Integer idFermata;
    String nomeLinea;
    Boolean verso;
    Boolean presoInCarico;  //todo
    Boolean arrivatoScuola; //todo


    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, String nomeLinea, Date data) {
        this.data = data;
        this.nomeLinea = nomeLinea;

        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        idChild = prenotazioneResource.getIdChild();
    }

    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity) {
        this.data = prenotazioneEntity.getData();
        this.nomeLinea = prenotazioneEntity.getNomeLinea();
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        idChild = prenotazioneEntity.getIdChild();
    }


}
