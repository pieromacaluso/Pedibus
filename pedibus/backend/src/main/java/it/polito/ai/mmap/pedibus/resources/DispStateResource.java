package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DispStateResource {
    private Boolean isConfirmed;
    private Boolean isAck;

    public DispStateResource(DispEntity d) {
        this.isConfirmed = d.getIsConfirmed();
        this.isAck = d.getIsAck();
    }

    public DispStateResource(DispAllResource d) {
        this.isConfirmed = d.getIsConfirmed();
        this.isAck = d.getIsAck();
    }
}
