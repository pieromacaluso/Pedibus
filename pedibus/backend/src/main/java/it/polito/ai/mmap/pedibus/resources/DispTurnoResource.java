package it.polito.ai.mmap.pedibus.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispTurnoResource {
    DispAllResource disp;
    TurnoResource turno;


}
