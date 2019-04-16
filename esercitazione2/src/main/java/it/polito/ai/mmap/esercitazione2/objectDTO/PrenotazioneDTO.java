package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.PrenotazioneEntity;
import it.polito.ai.mmap.esercitazione2.resources.PrenotazioneResource;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PrenotazioneDTO {
    String nomeAlunno;
    String data;
    Integer idFermata;
    Boolean verso;
    LineaDTO lineaDTO;          //todo utile avere tutti i dettagli della linea dietro? valutare se utile sostituire con l'id della linea




    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, LineaDTO lineaDTO, String data) {
        this.data = data;
        this.lineaDTO = lineaDTO;
        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        nomeAlunno = prenotazioneResource.getNomeAlunno();
    }
    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity, LineaDTO lineaDTO) {
        this.data = prenotazioneEntity.getData();
        this.lineaDTO = lineaDTO;
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        nomeAlunno = prenotazioneEntity.getNomeAlunno();
    }


}
