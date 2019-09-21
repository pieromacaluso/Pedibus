package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.services.LineeService;
import it.polito.ai.mmap.pedibus.services.ReservationService;
import it.polito.ai.mmap.pedibus.services.UserService;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//TODO in teoria non è più usato, restituiamo un List<childDTO> direttamente e il costruttore è stato incorporato in reservationService.getChildrenNotReserved
@Data
public class GetChildrenNotReservedLineaDataResource {

    List<ChildDTO> childrenNotReserved;

    public GetChildrenNotReservedLineaDataResource(Date data, boolean verso, ReservationService reservationService, UserService userService){
        List<String> tmp;

        List<String> bambiniDataVerso=reservationService.getAllChildrenForReservationDataVerso(data,verso);
        List<String> bambini=userService.getAllChildrenId();                                                    //tutti i bambini iscritti
        tmp=bambini.stream().filter(bambino->!bambiniDataVerso.contains(bambino)).collect(Collectors.toList());     //tutti i bambini iscritti tranne quelli che si sono prenotati per quel giorno linea e verso

        childrenNotReserved=userService.getAllChildrenById(tmp);
    }

}
