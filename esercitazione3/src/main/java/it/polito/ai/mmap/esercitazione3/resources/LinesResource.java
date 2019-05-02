package it.polito.ai.mmap.esercitazione3.resources;

import it.polito.ai.mmap.esercitazione3.objectDTO.LineaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /lines
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LinesResource extends ResourceSupport {


    ArrayList<String> listLineeName = new ArrayList<>();

    public LinesResource(ArrayList<LineaDTO> listLineaDTO) {
        listLineeName.addAll(listLineaDTO.stream().map(LineaDTO::getNome).collect(Collectors.toList()));
    }
}
