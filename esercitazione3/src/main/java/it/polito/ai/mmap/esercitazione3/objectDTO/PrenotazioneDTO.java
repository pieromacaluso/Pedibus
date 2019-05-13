package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione3.resources.PrenotazioneResource;
import lombok.Data;

import java.util.Date;

@Data
public class PrenotazioneDTO {
    String nomeAlunno;
    Date data;
    Integer idFermata;
    String nomeLinea;
    Boolean verso;





    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, String nomeLinea, Date data) {
        this.data = data;
        this.nomeLinea = nomeLinea;

        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();
    }
    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity) {
        this.data = prenotazioneEntity.getData();
        this.nomeLinea = prenotazioneEntity.getNomeLinea();
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        nomeAlunno = prenotazioneEntity.getNomeAlunno();
    }


}
