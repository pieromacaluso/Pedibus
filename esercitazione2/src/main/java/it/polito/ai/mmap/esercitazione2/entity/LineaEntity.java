package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@Document(collection = "lines")
public class LineaEntity {
    @Id
    private float id;
    private String nome;
    private String admin;

    //TODO devono diventare liste di idFermata
    ArrayList<FermataDTO> andata = new ArrayList<>();
    ArrayList<FermataDTO> ritorno = new ArrayList<>();
}
