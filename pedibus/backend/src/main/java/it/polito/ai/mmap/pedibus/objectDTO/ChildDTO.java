package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
public class ChildDTO {

    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;
    private Integer idFermataAndata;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare
    private Integer idFermataRitorno;
    private ObjectId idParent;

    public ChildDTO(ChildEntity childEntity){
        codiceFiscale=childEntity.getCodiceFiscale();
        name=childEntity.getName();
        surname=childEntity.getSurname();
        idFermataAndata =childEntity.getIdFermataAndata();
        idFermataRitorno = childEntity.getIdFermataRitorno();
        idParent=childEntity.getIdParent();
    }
}


