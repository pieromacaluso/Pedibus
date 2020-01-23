package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.entity.ReservationEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ReservationsDump {
    private Date data;
    private String idLinea;
    private boolean verso;
    List<Reservation> reservationList;
    List<String> cfAbsentChild;

    public ReservationsDump(Date data, String idLinea, boolean verso, List<ReservationEntity> reservationEntityList, List<String> cfAbsentChild) {
        this.data = data;
        this.idLinea = idLinea;
        this.verso = verso;
        reservationList = reservationEntityList.stream().map(Reservation::new).collect(Collectors.toList());
        this.cfAbsentChild = cfAbsentChild;
    }

    @Data
    @NoArgsConstructor
    public class Reservation {
        private String cfChild;
        private Integer idFermata;
        private boolean presoInCarico;
        private boolean arrivatoScuola;
        private boolean assente;
        private Date presoInCaricoDate;
        private Date arrivatoScuolaDate;
        private Date assenteDate;

        public Reservation(ReservationEntity reservationEntity) {
            cfChild = reservationEntity.getCfChild();
            idFermata = reservationEntity.getIdFermata();
            presoInCarico = reservationEntity.isPresoInCarico();
            arrivatoScuola = reservationEntity.isArrivatoScuola();
            assente = reservationEntity.isAssente();
            presoInCaricoDate = reservationEntity.getPresoInCaricoDate();
            arrivatoScuolaDate = reservationEntity.getArrivatoScuolaDate();
            assenteDate = reservationEntity.getAssenteDate();
        }
    }
}
