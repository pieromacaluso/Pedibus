package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.PrenotazioneEntity;
import it.polito.ai.mmap.pedibus.resources.PrenotazioneResource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class PrenotazioneDTO {

    private String cfChild;
    private Date data;
    private String nomeLinea;
    private Integer idFermata;
    private Boolean verso;
    private Boolean presoInCarico;
    private Boolean arrivatoScuola;

    public PrenotazioneDTO(PrenotazioneResource prenotazioneResource, String nomeLinea, Date data) {
        this.data = data;
        this.nomeLinea = nomeLinea;
        verso = prenotazioneResource.getVerso();
        idFermata = prenotazioneResource.getIdFermata();
        cfChild = prenotazioneResource.getCfChild();
        // TODO: check
        presoInCarico = false;
        arrivatoScuola = false;
    }

    public PrenotazioneDTO(PrenotazioneEntity prenotazioneEntity) {
        this.data = prenotazioneEntity.getData();
        this.nomeLinea = prenotazioneEntity.getNomeLinea();
        verso = prenotazioneEntity.isVerso();
        idFermata = prenotazioneEntity.getIdFermata();
        cfChild = prenotazioneEntity.getCfChild();
        this.presoInCarico = prenotazioneEntity.isPresoInCarico();
        this.arrivatoScuola = prenotazioneEntity.isArrivatoScuola();
    }


}
