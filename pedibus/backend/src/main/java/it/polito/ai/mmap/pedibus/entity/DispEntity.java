package it.polito.ai.mmap.pedibus.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "disp")
public class DispEntity {
    @Id
    private ObjectId dispId;

    private String guideUsername;
    private Integer idFermata;
    private ObjectId turnoId;
    private Boolean isConfirmed;


    public DispEntity(String guideUsername,Integer idFermata, ObjectId turnoId)
    {
        this.guideUsername = guideUsername;
        this.idFermata = idFermata;
        this.turnoId = turnoId;
    }
}
