package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import it.polito.ai.mmap.pedibus.resources.ReservationResource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ReservationDTO {

    private String id;
    private String cfChild;
    private Date data;
    private String idLinea;
    private Integer idFermata;
    private Boolean verso;
    private Boolean presoInCarico;
    private Boolean arrivatoScuola;
    private Boolean assente;
    private Date presoInCaricoDate;
    private Date arrivatoScuolaDate;
    private Date assenteDate;
    private String presoInCaricoNotifica;
    private String arrivatoScuolaNotifica;
    private String assenteNotifica;

    public ReservationDTO(ReservationResource reservationResource, String idLinea, Date data) {
        this.data = data;
        this.idLinea = idLinea;
        verso = reservationResource.getVerso();
        idFermata = reservationResource.getIdFermata();
        cfChild = reservationResource.getCfChild();
        presoInCarico = false;
        arrivatoScuola = false;
        assente = false;
        presoInCaricoDate = null;
        arrivatoScuolaDate = null;
        assenteDate = null;
        presoInCaricoNotifica = null;
        arrivatoScuolaNotifica = null;
        assenteNotifica = null;
    }

    public ReservationDTO(ReservationEntity reservationEntity) {
        this.id = reservationEntity.getId().toString();
        this.data = reservationEntity.getData();
        this.idLinea = reservationEntity.getIdLinea();
        verso = reservationEntity.isVerso();
        idFermata = reservationEntity.getIdFermata();
        cfChild = reservationEntity.getCfChild();
        this.presoInCarico = reservationEntity.isPresoInCarico();
        this.arrivatoScuola = reservationEntity.isArrivatoScuola();
        this.assente = reservationEntity.isAssente();
        this.presoInCaricoDate = reservationEntity.getPresoInCaricoDate();
        this.arrivatoScuolaDate = reservationEntity.getArrivatoScuolaDate();
        this.assenteDate = reservationEntity.getAssenteDate();
        presoInCaricoNotifica = reservationEntity.getPresoInCaricoNotifica();
        arrivatoScuolaNotifica = reservationEntity.getArrivatoScuolaNotifica();
        assenteNotifica = reservationEntity.getAssenteNotifica();
    }


}
