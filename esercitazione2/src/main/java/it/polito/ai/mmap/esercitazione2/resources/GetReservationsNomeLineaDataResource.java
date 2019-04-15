package it.polito.ai.mmap.esercitazione2.resources;


import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.services.LineService;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
public class GetReservationsNomeLineaDataResource extends ResourceSupport {

    LineService lineService;
    List<FermataDTOAlunni> fermataDTOAlunniAndata=new ArrayList<>();
    List<FermataDTOAlunni> fermataDTOAlunniRitorno=new ArrayList<>();

    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        ArrayList<String> alunni=new ArrayList<>();

        public FermataDTOAlunni(FermataDTO f) {
            fermata=f;
        }
    }

    /*public GetReservationsNomeLineaDataResource(String nomeLina,String data){
        List<FermataDTO> fermateAndata=lineService.getLine(nomeLina).getAndata();
        fermateAndata.stream().map(fermataDTO -> new FermataDTOAlunni(fermataDTO)).forEach((f)->{
                f.setAlunni(todo implementare ricerca in mongo di tutti i bambini presenti in quella data in quella fermata);
        });
    }*/
}
