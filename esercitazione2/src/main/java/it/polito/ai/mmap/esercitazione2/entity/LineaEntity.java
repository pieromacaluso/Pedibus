package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import it.polito.ai.mmap.esercitazione2.objectDTO.LineaDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "lines")
public class LineaEntity {

    @Id
    private Integer id;
    private String nome;
    private String admin;
    List<Integer> andata;
    List<Integer> ritorno;

    public LineaEntity() {

    }

    /*  TODO: trovare modo piu' leggibile */

    public LineaEntity(LineaDTO lineaDTO) {
        this.id = lineaDTO.getId();
        this.nome = lineaDTO.getNome();
        this.admin = lineaDTO.getAdmin();
        andata = new ArrayList<>();
        ritorno = new ArrayList<>();
        for (FermataDTO dto1 : lineaDTO.getAndata())
            andata.add((int) dto1.getId());
        for (FermataDTO dto2 : lineaDTO.getRitorno())
            ritorno.add((int) dto2.getId());
    }

}
