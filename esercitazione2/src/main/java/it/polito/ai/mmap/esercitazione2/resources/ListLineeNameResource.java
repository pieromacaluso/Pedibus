package it.polito.ai.mmap.esercitazione2.resources;

import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /lines
 *
 */
@Data
public class ListLineeNameResource extends ResourceSupport {
    ArrayList<String> listLineeName = new ArrayList<>();

    public ListLineeNameResource(ArrayList<LineaDTO> listLineaDTO) {
        listLineeName.addAll(listLineaDTO.stream().map(lineaDTO -> lineaDTO.getNome()).collect(Collectors.toList()));
    }
}
