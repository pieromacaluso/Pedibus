package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PrenotazioneDTO {
    LocalDateTime data;
    LineaDTO lineaDTO;
    Boolean verso;
    Integer idFermata;
    String nomeAlunno;

    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, LineaDTO lineaDTO, LocalDateTime data) {
        this.data = data;
        this.lineaDTO = lineaDTO;
        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();

    }
}
