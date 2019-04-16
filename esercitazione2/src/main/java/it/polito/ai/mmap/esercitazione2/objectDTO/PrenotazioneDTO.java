package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import lombok.Data;


@Data
public class PrenotazioneDTO {
    String nomeAlunno;
    String data;
    Integer idFermata;
    Integer idLinea;
    Boolean verso;

    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, Integer idLinea, String data) {
        this.data = data;
        this.idLinea = idLinea;
        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();
    }
    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity, Integer idLinea) {
        this.data = prenotazioneEntity.getData();
        this.idLinea = idLinea;
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        nomeAlunno = prenotazioneEntity.getNomeAlunno();
    }


}
