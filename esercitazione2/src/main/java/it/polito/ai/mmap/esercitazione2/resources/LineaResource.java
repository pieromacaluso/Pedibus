package it.polito.ai.mmap.esercitazione2.resources;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import java.util.ArrayList;


@Data
public class LineaResource extends ResourceSupport {
    ArrayList<FermataDTO> andata;
    ArrayList<FermataDTO> ritorno;

    public LineaResource(LineaDTO lineaDTO) {
        this.andata = lineaDTO.getAndata();
        this.ritorno = lineaDTO.getRitorno();
    }
}
