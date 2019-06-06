package it.polito.ai.mmap.pedibus.resources;


import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
@Data
public class GetReservationsNomeLineaDataResource {


    List<FermataDTOAlunni> alunniPerFermataAndata;
    List<FermataDTOAlunni> alunniPerFermataRitorno;

    public GetReservationsNomeLineaDataResource(String nomeLina, Date data, LineeService lineeService, ReservationService reservationService) {
        // Ordinati temporalmente, quindi seguendo l'andamento del percorso
        alunniPerFermataAndata = (lineeService.getLineByName(nomeLina)).getAndata().stream()
                .map(FermataDTOAlunni::new)
                .collect(Collectors.toList());
        // true per indicare l'andata
        alunniPerFermataAndata.forEach((f) -> f.setAlunni(reservationService.findAlunniFermata(data, f.getFermata().getId(), true)));


        alunniPerFermataRitorno = (lineeService.getLineByName(nomeLina)).getRitorno().stream()
                .map(FermataDTOAlunni::new)
                .collect(Collectors.toList());
        // false per indicare il ritorno
        alunniPerFermataRitorno.forEach((f) -> f.setAlunni(reservationService.findAlunniFermata(data, f.getFermata().getId(), false)));
    }

    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        List<ChildEntity> alunni;

        FermataDTOAlunni(FermataDTO f) {
            fermata = f;
        }
    }
}
