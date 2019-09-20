package it.polito.ai.mmap.pedibus.objectDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polito.ai.mmap.pedibus.entity.FermataEntity;
import it.polito.ai.mmap.pedibus.services.MongoZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonIgnore
    public Date getDateOrario()
    {
        return MongoZonedDateTime.getMongoZonedDateTimeFromTime(orario);
    }

}