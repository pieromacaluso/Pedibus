package it.polito.ai.mmap.pedibus.objectDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DispDTO {
//    private String guideUsername; lo recuperiamo dal principal
    private Integer idFermata;
    private TurnoDTO turnoDTO;
}
