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

    private Boolean isOpen; //true aperto, false chiuso
    private Boolean isExpired;

    public TurnoResource(TurnoEntity turno){
        this.isOpen = turno.getIsOpen();
        this.isExpired = turno.getIsExpired();
    }
}
