package it.polito.ai.mmap.esercitazione2.entity;

import it.polito.ai.mmap.esercitazione2.objectDTO.FermataDTO;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "stops")
public class FermataEntity {
    @Transient
    public static final String SEQUENCE_NAME = "stop_sequence";
    @Id
    private String id;

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
        this.verso = fermataDTO.getVerso();
        this.nomeLinea = fermataDTO.getNomeLinea();
    }

}
