package it.polito.ai.mmap.pedibus.objectDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TurnoDTO {
    private String idLinea;
    private Date data;
    private Boolean verso;
}
