package it.polito.ai.mmap.pedibus.objectDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class DispDTO {
    private String guideUsername;
    private TurnoDTO turnoDTO;
}
