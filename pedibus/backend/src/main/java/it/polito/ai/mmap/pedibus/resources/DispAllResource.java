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
    private String idLinea;
    private String nomeLinea;
    private Integer idFermata;
    private String nomeFermata;
    private Boolean isConfirmed; //non cancellare
    private Boolean isAck;

    public DispAllResource(DispEntity e, String nomeFermata, String nomeLinea) {
        guideUsername = e.getGuideUsername();
        idLinea = e.getIdLinea();
        idFermata = e.getIdFermata();
        this.nomeLinea = nomeLinea;
        this.nomeFermata = nomeFermata;
        isConfirmed = e.getIsConfirmed();
        isAck = e.getIsAck();
    }
}
