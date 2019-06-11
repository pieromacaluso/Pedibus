package it.polito.ai.mmap.pedibus.resources;

import lombok.Data;
import org.bson.types.ObjectId;

/**
 * Ci servirà per restituirla all'interno di GetReservationsNomeLineaDataResource
 */
@Data
public class ChildResource {

    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;
    private ObjectId idParent;
}
