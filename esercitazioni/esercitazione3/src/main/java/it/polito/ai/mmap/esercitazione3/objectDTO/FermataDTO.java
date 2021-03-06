package it.polito.ai.mmap.esercitazione3.objectDTO;

import it.polito.ai.mmap.esercitazione3.entity.FermataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FermataDTO {

    private Integer id;
    private String nome;
    private String orario;

    /**
     * Conversione da Entity a DTO
     *
     * @param fermataEntity
     */
    public FermataDTO(FermataEntity fermataEntity) {
        id = fermataEntity.getId();
        nome = fermataEntity.getName();
        orario = fermataEntity.getOrario();
    }

}