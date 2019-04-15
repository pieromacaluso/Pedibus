package it.polito.ai.mmap.esercitazione2.resources;


import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import it.polito.ai.mmap.esercitazione2.services.LineService;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
public class GetReservationsNomeLineaDataResource extends ResourceSupport {

    LineService lineService;
    MongoService mongoService;


    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        List<String> alunni=new ArrayList<>();

        public FermataDTOAlunni(FermataDTO f) {
            fermata=f;
        }
    }

    public GetReservationsNomeLineaDataResource(String nomeLina,String data){
        LineaDTO linea=lineService.getLine(nomeLina);                   //todo non funziona e check nomelinea non esistente
        List<FermataDTO> fermateAndata=linea.getAndata();
        fermateAndata.stream().map(fermataDTO -> new FermataDTOAlunni(fermataDTO)).forEach((f)->{
                f.setAlunni(mongoService.findAlunniFermata(data,f.getFermata().getId(),true));    //true per indicare l'andata
        });
        List<FermataDTO> fermateRitorno=lineService.getLine(nomeLina).getRitorno();
        fermateAndata.stream().map(fermataDTO -> new FermataDTOAlunni(fermataDTO)).forEach((f)->{
            f.setAlunni(mongoService.findAlunniFermata(data,f.getFermata().getId(),false));    //true per indicare l'andata
        });
    }
}
