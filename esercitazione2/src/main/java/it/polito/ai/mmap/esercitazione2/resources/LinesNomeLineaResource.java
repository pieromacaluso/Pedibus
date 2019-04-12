package it.polito.ai.mmap.esercitazione2.resources;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import java.util.ArrayList;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /lines/{nome_linea}
 */

@Data
public class LinesNomeLineaResource extends ResourceSupport {
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    public LinesNomeLineaResource(LineaDTO lineaDTO) {
        this.andata = lineaDTO.getAndata();
        this.ritorno = lineaDTO.getRitorno();
    }
}
