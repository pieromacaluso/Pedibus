package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}/{verso}
 */
@Data
@NoArgsConstructor
public class GetReservationsIdDataVersoResource {
    List<FermataAlunniResource> alunniPerFermata;
    String orarioScuola;
    List<ChildDTO> childrenNotReserved;
    Boolean canModify;
}
