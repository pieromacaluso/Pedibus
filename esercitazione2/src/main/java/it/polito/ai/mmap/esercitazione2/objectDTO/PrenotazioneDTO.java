package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import lombok.Data;

import java.util.Date;

@Data
public class PrenotazioneDTO {
    Date data;
    LineaDTO lineaDTO;
    Boolean verso;
    Integer idFermata;
    String nomeAlunno;

    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, LineaDTO lineaDTO, Date data) {
        this.data = data;
        this.lineaDTO = lineaDTO;
        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();

    }
}
