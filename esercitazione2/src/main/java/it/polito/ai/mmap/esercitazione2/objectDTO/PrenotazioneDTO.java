package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import lombok.Data;
import java.util.Date;

@Data
public class PrenotazioneDTO {
    String nomeAlunno;
    Date data;
    Integer idFermata;
    Integer idLinea;
    Boolean verso;





    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, Integer idLinea, Date data) {
        this.data = data;
        this.idLinea = idLinea;

        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();
    }
    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity) {
        this.data = prenotazioneEntity.getData();
        this.idLinea = prenotazioneEntity.getIdLinea();
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        nomeAlunno = prenotazioneEntity.getNomeAlunno();
    }


}
