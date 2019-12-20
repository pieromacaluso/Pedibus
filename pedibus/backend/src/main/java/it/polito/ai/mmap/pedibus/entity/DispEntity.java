package it.polito.ai.mmap.pedibus.entity;

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
@Document(collection = "disp")
public class DispEntity {
    @Id
    private ObjectId dispId;

    private String guideUsername;
    private String idLinea;
    private Integer idFermata;
    private ObjectId turnoId;
    private Boolean isConfirmed;
    private Boolean isAck;
    private Date dataAck;

    public DispEntity(String guideUsername, String idLinea, Integer idFermata, ObjectId turnoId) {
        this.guideUsername = guideUsername;
        this.idLinea = idLinea;
        this.idFermata = idFermata;
        this.turnoId = turnoId;
        isConfirmed = false;
        isAck = false;
    }
}
