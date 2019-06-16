package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.objectDTO.LineaDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}/{verso}
 */
@Data
public class GetReservationsNomeLineaDataVersoResource {
    List<FermataDTOAlunni> alunniPerFermata;
    String arrivoScuola;
    String partenzaScuola;
    List<ChildDTO> childrenNotReserved;
    Boolean canModify;


    public GetReservationsNomeLineaDataVersoResource(String nomeLina, Date data, LineeService lineeService, UserService userService, ReservationService reservationService, boolean verso, boolean canModify) {
        // Ordinati temporalmente, quindi seguendo l'andamento del percorso
        ArrayList<FermataDTO> fermate;
        if (verso)
            fermate = lineeService.getLineById(nomeLina).getAndata();
        else
            fermate = lineeService.getLineById(nomeLina).getRitorno();

        alunniPerFermata = fermate.stream()
                .map(FermataDTOAlunni::new)
                .collect(Collectors.toList());
        alunniPerFermata.forEach((f) -> f.setAlunni(reservationService.findAlunniFermata(data, f.getFermata().getId(), verso)));


        List<String> tmp;

        List<String> bambiniDataVerso = reservationService.getAllChildrenForReservationDataVerso(data, verso);
        List<String> bambini = userService.getAllChildrenId();                                                    //tutti i bambini iscritti
        tmp = bambini.stream().filter(bambino -> !bambiniDataVerso.contains(bambino)).collect(Collectors.toList());     //tutti i bambini iscritti tranne quelli che si sono prenotati per quel giorno linea e verso

        childrenNotReserved = userService.getAllChildrenById(tmp);

        this.canModify = canModify;
        this.arrivoScuola = lineeService.getArrivoScuola();
        this.partenzaScuola = lineeService.getPartenzaScuola();
    }

    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        List<PrenotazioneChildResource> alunni;

        FermataDTOAlunni(FermataDTO f) {
            fermata = f;
        }
    }
}
