package it.polito.ai.mmap.pedibus.resources;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.PrenotazioneEntity;
import it.polito.ai.mmap.pedibus.resources.PrenotazioneResource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class PrenotazioneChildResource {

    private String codiceFiscale;
    private String name;
    private String surname;
    private Boolean presoInCarico;
    private Boolean arrivatoScuola;

    public PrenotazioneChildResource(PrenotazioneEntity p, ChildEntity c){
        this.codiceFiscale = p.getCfChild();
        this.name = c.getName();
        this.surname = c.getSurname();
        this.presoInCarico = p.isPresoInCarico();
        this.arrivatoScuola = p.isArrivatoScuola();

    }


}
