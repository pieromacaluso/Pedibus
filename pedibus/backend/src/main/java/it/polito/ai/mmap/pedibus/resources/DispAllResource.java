package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.DispEntity;
import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import it.polito.ai.mmap.pedibus.entity.LineaEntity;
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
    private String orario;
    private Boolean isConfirmed; //non cancellare
    private Boolean isAck;

    public DispAllResource(DispEntity e, FermataEntity f, LineaEntity l) {
        guideUsername = e.getGuideUsername();
        idLinea = e.getIdLinea();
        idFermata = e.getIdFermata();
        this.nomeLinea = l.getNome();
        this.nomeFermata = f.getName();
        this.orario = f.getOrario();
        isConfirmed = e.getIsConfirmed();
        isAck = e.getIsAck();
    }
}
