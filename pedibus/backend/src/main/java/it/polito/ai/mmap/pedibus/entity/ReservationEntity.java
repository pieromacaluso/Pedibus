package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.ReservationDTO;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "reservations")
public class ReservationEntity {

    @Id
    private ObjectId id;
    private String cfChild;
    private Date data;
    private String idLinea;
    private Integer idFermata;
    private boolean verso;
    private boolean presoInCarico;
    private boolean arrivatoScuola;

    public ReservationEntity(ReservationDTO reservationDTO) {
        cfChild = reservationDTO.getCfChild();
        data = reservationDTO.getData();
        verso = reservationDTO.getVerso();
        idFermata = reservationDTO.getIdFermata();
        idLinea = reservationDTO.getIdLinea();
        this.presoInCarico = reservationDTO.getPresoInCarico();
        this.arrivatoScuola = reservationDTO.getArrivatoScuola();
    }

    public void update(ReservationDTO reservationDTO) {
        //non viene modificato l'id perchè si vuole solo aggiornare i campi della stessa reservation
        this.cfChild = reservationDTO.getCfChild();
        this.data = reservationDTO.getData();
        this.verso = reservationDTO.getVerso();
        this.idFermata = reservationDTO.getIdFermata();
        this.idLinea = reservationDTO.getIdLinea();
        this.presoInCarico = reservationDTO.getPresoInCarico();
        this.arrivatoScuola = reservationDTO.getArrivatoScuola();
    }


    public boolean equalsResource(ReservationResource reservationResource) {
        return this.cfChild.equals(reservationResource.getCfChild()) && this.verso == reservationResource.getVerso() && this.idFermata.equals(reservationResource.getIdFermata());
    }
}
