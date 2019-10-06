package it.polito.ai.mmap.pedibus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polito.ai.mmap.pedibus.objectDTO.FermataDTO;
import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@NoArgsConstructor
@Document(collection = "stops")
public class FermataEntity {

    @Id
    private Integer id;

    private String name;
    private String orario;
    private String idLinea;
    private Boolean verso;

    /**
     * Conversione da DTO ad Entity
     * @param fermataDTO
     */
    public FermataEntity(FermataDTO fermataDTO) {
        this.id = fermataDTO.getId();
        this.name = fermataDTO.getNome();
        this.orario = fermataDTO.getOrario();
    }
    @JsonIgnore
    public Date getDateOrario()
    {
        return MongoZonedDateTime.getMongoZonedDateTimeFromTime(orario);
    }

}
