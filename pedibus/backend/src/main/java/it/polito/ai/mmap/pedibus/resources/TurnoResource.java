package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TurnoResource {

    private String idLinea;
    private Date data;
    private Boolean verso;
    private Boolean isOpen; //true aperto, false chiuso
    private Boolean isExpired;

    public TurnoResource(TurnoEntity turno){
        this.idLinea = turno.getIdLinea();
        this.data = turno.getData();
        this.verso = turno.getVerso();
        this.isOpen = turno.getIsOpen();
        this.isExpired = turno.getIsExpired();
    }
}
