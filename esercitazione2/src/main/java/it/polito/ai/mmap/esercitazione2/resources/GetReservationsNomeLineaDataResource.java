package it.polito.ai.mmap.esercitazione2.resources;


import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.services.MongoService;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe che mappa da java a json l'oggetto chiesto da GET /reservations/{nome_linea}/{data}
 */
@Data
public class GetReservationsNomeLineaDataResource extends ResourceSupport {

    List<FermataDTOAlunni> alunniPerFermataAndata=new ArrayList<>();
    List<FermataDTOAlunni> alunniPerFermataRitorno=new ArrayList<>();

    @Data
    public static class FermataDTOAlunni {
        FermataDTO fermata;
        List<String> alunni=new ArrayList<>();

        public FermataDTOAlunni(FermataDTO f) {
            fermata=f;
        }
    }

    public GetReservationsNomeLineaDataResource(String nomeLina,String data, MongoService mongoService){

        //LineaDTO linea=lineService.getLine(nomeLina);                   //todo check nomelinea non esistente

        alunniPerFermataAndata=(mongoService.getLine(nomeLina)).getAndata().stream() //ordinati temporalmente, quindi seguendo l'andamento del percorso
                .map(fermataDTO -> new FermataDTOAlunni(fermataDTO))
                .collect(Collectors.toList());
        alunniPerFermataAndata.forEach((f)->{
                f.setAlunni(mongoService.findAlunniFermata(data,f.getFermata().getId(),true));    //true per indicare l'andata
        });

        alunniPerFermataRitorno=(mongoService.getLine(nomeLina)).getRitorno().stream()
                .map(fermataDTO -> new FermataDTOAlunni(fermataDTO))
                .collect(Collectors.toList());
        alunniPerFermataRitorno.forEach((f)->{
                    f.setAlunni(mongoService.findAlunniFermata(data,f.getFermata().getId(),false));    //false per indicare il ritorno
                });

        System.out.println("Studenti per fermate ottenuti");
    }
}
