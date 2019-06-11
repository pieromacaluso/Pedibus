package it.polito.ai.mmap.pedibus.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HandledResource {
    private String cfChild;
    private Boolean isSet;
    private Integer idFermata;
}
