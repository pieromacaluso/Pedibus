package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Classe che mappa l'oggetto chiesto da GET /lines
 *
 */
@Data
public class LinesResource {


    ArrayList<String> listLineeName = new ArrayList<>();

    public LinesResource(ArrayList<LineaDTO> listLineaDTO) {
        listLineeName.addAll(listLineaDTO.stream().map(LineaDTO::getNome).collect(Collectors.toList()));
    }
}
