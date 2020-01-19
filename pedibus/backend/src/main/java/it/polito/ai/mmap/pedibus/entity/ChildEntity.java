package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "children")
public class ChildEntity {

    @Id
    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;
    private Integer idFermataAndata;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare
    private Integer idFermataRitorno;

    public ChildEntity(ChildDTO childDTO) {
        codiceFiscale = childDTO.getCodiceFiscale();
        name = childDTO.getName();
        surname = childDTO.getSurname();
        idFermataAndata = childDTO.getIdFermataAndata();
        idFermataRitorno = childDTO.getIdFermataRitorno();
    }

    public ChildEntity(ChildDTO childDTO, ObjectId idPar) {
        codiceFiscale = childDTO.getCodiceFiscale();
        name = childDTO.getName();
        surname = childDTO.getSurname();
        idFermataAndata = childDTO.getIdFermataAndata();
        idFermataRitorno = childDTO.getIdFermataRitorno();
    }

    public ChildEntity(String cf, String name, String sur) {
        codiceFiscale = cf;
        this.name = name;
        surname = sur;
    }

}


