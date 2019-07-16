package it.polito.ai.mmap.pedibus.resources;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ReservationChildResource {

    private String codiceFiscale;
    private String name;
    private String surname;
    private Boolean presoInCarico;
    private Boolean arrivatoScuola;

    public ReservationChildResource(ReservationEntity p, ChildEntity c){
        this.codiceFiscale = p.getCfChild();
        this.name = c.getName();
        this.surname = c.getSurname();
        this.presoInCarico = p.isPresoInCarico();
        this.arrivatoScuola = p.isArrivatoScuola();

    }


}
