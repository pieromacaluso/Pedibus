package it.polito.ai.mmap.esercitazione2.objectDTO;

import it.polito.ai.mmap.esercitazione2.entity.FermataEntity;
import lombok.Data;

@Data
public class FermataDTO {
    private Integer id;
    private String nome;
    private String orario;

    public FermataDTO(){}

    /**
     * Conversione da Entity a DTO
     * @param fermataEntity
     */
    public FermataDTO(FermataEntity fermataEntity){
        id=fermataEntity.getId();
        nome=fermataEntity.getName();
        orario=fermataEntity.getOrario();
    }

}