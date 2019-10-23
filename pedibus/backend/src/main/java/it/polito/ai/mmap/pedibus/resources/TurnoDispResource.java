package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.TurnoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TurnoDispResource {
    private Boolean isOpen; //true aperto, false chiuso
    private Boolean isExpired;
    private Map<String, List<DispAllResource>> listDisp;

    public TurnoDispResource(TurnoEntity turno, Map<String, List<DispAllResource>> dispResourceMap){
        this.isOpen = turno.getIsOpen();
        this.isExpired = turno.getIsExpired();
        this.listDisp = dispResourceMap;
    }
}
