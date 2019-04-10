package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "stops")
public class FermataEntity {
    @Id
    private float id;
    private String name;
    private String orario;

    public FermataEntity() {

    }

    public FermataEntity(FermataDTO fermataDTO) {
        this.id = fermataDTO.getId();
        this.name = fermataDTO.getNome();
        this.orario = fermataDTO.getOrario();
    }

}
