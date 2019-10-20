package it.polito.ai.mmap.pedibus.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class DispAllResource {
    private String guideUsername;
    private Integer idFermata;
    private String nomeFermata;
    private Boolean isConfirmed; //non cancellare
}
