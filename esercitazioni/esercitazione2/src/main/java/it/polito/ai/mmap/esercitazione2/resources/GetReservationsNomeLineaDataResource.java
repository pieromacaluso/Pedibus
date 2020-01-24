package it.polito.ai.mmap.esercitazione2.resources;


import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetReservationsNomeLineaDataResource extends ResourceSupport {

    List<FermataDTOAlunni> alunniPerFermataAndata;
    List<FermataDTOAlunni> alunniPerFermataRitorno;

    public GetReservationsNomeLineaDataResource(String nomeLina, Date data, MongoService mongoService) {
        // Ordinati temporalmente, quindi seguendo l'andamento del percorso
        alunniPerFermataAndata = (mongoService.getLineByName(nomeLina)).getAndata().stream()
                .map(FermataDTOAlunni::new)
                .collect(Collectors.toList());
        // true per indicare l'andata
        alunniPerFermataAndata.forEach((f) -> f.setAlunni(mongoService.findAlunniFermata(data, f.getFermata().getId(), true)));

        alunniPerFermataRitorno = (mongoService.getLineByName(nomeLina)).getRitorno().stream()
                .map(FermataDTOAlunni::new)
                .collect(Collectors.toList());
        // false per indicare il ritorno
        alunniPerFermataRitorno.forEach((f) -> f.setAlunni(mongoService.findAlunniFermata(data, f.getFermata().getId(), false)));
    }

    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        List<String> alunni = new ArrayList<>();

        FermataDTOAlunni(FermataDTO f) {
            fermata = f;
        }
    }
}
