package it.polito.ai.mmap.pedibus.resources;

import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public  class FermataAlunniResource {
    FermataDTO fermata;
    List<ReservationChildResource> alunni;

    public FermataAlunniResource(FermataDTO fermataDTO) {
        fermata = fermataDTO;
    }
}