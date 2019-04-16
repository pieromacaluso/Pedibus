package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
public class FermataDTO {

    private String id;
    private String nome;
    private String orario;
    private String nomeLinea;
    private Boolean verso;

    public FermataDTO() {
    }

    /**
     * Conversione da Entity a DTO
     *
     * @param fermataEntity
     */
    public FermataDTO(FermataEntity fermataEntity) {
        id = fermataEntity.getId();
        nome = fermataEntity.getName();
        orario = fermataEntity.getOrario();
        this.verso = fermataEntity.getVerso();
        this.nomeLinea = fermataEntity.getNomeLinea();
    }

}