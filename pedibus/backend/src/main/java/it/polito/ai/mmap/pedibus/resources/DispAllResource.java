package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class DispAllResource {
    private String guideUsername;
    private String fermata;
    private Boolean isConfirmed;

}
