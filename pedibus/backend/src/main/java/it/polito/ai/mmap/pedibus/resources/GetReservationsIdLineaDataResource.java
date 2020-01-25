package it.polito.ai.mmap.pedibus.resources;


import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe che mappa l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
@Data
@NoArgsConstructor
public class GetReservationsIdLineaDataResource {
    List<FermataAlunniResource> alunniPerFermataAndata;
    List<FermataAlunniResource> alunniPerFermataRitorno;
    String arrivoScuola;
    String partenzaScuola;
    Boolean canModify;
}
