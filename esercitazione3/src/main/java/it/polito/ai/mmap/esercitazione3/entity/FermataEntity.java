package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.objectDTO.FermataDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "stops")
public class FermataEntity {

    @Id
    private Integer id;

    private String name;
    private String orario;
    private String nomeLinea;
    private Boolean verso;

    public FermataEntity() {

    }

    /**
     * Conversione da DTO ad Entity
     * @param fermataDTO
     */
    public FermataEntity(FermataDTO fermataDTO) {
        this.id = fermataDTO.getId();
        this.name = fermataDTO.getNome();
        this.orario = fermataDTO.getOrario();
    }

}
