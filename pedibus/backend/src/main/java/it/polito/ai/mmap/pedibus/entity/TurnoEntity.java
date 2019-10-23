package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.TurnoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "turni")
public class TurnoEntity {
    @Id
    private ObjectId turnoId;
    private String idLinea;
    private Date data;
    private Boolean verso;
    private Boolean isOpen; //true aperto, false chiuso
    private Boolean isExpired;

    public TurnoEntity(TurnoDTO turnoDTO)
    {
        this.idLinea = turnoDTO.getIdLinea();
        this.data = turnoDTO.getData();
        this.verso = turnoDTO.getVerso();
        isOpen = true;
        isExpired = false;
    }

}
