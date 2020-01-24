package it.polito.ai.mmap.esercitazione2.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Prenotazione {
    private String nomeAlunno;
    private Integer idFermata;
    private Boolean verso;
}
