package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class ChildDTO {

    @Pattern(regexp = "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    @Required
    private String name;
    @Required
    private String surname;
    @Required
    private Integer idFermataAndata;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare
    @Required
    private Integer idFermataRitorno;

    public ChildDTO(ChildEntity childEntity){
        codiceFiscale=childEntity.getCodiceFiscale();
        name=childEntity.getName();
        surname=childEntity.getSurname();
        idFermataAndata =childEntity.getIdFermataAndata();
        idFermataRitorno = childEntity.getIdFermataRitorno();
    }
}


