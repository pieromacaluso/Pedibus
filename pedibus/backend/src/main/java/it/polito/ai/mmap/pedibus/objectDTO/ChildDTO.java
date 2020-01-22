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

    @Pattern(regexp = "^(?:[A-Z][AEIOU][AEIOUX]|[B-DF-HJ-NP-TV-Z]{2}[A-Z]){2}(?:[\\dLMNP-V]{2}(?:[A-EHLMPR-T](?:[04LQ][1-9MNP-V]|[15MR][\\dLMNP-V]|[26NS][0-8LMNP-U])|[DHPS][37PT][0L]|[ACELMRT][37PT][01LM]|[AC-EHLMPR-T][26NS][9V])|(?:[02468LNQSU][048LQU]|[13579MPRTV][26NS])B[26NS][9V])(?:[A-MZ][1-9MNP-V][\\dLMNP-V]{2}|[A-M][0L](?:[1-9MNP-V][\\dLMNP-V]|[0L][1-9MNP-V]))[A-Z]$", flags = Pattern.Flag.CASE_INSENSITIVE)
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


