package it.polito.ai.mmap.pedibus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import it.polito.ai.mmap.pedibus.objectDTO.ChildDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "children")
public class ChildEntity {

    @Id
    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;
    private Integer idFermataDefault;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare
    private ObjectId idParent;

    public ChildEntity(ChildDTO childDTO) {
        codiceFiscale = childDTO.getCodiceFiscale();
        name = childDTO.getName();
        surname = childDTO.getSurname();
        idFermataDefault = childDTO.getIdFermataDefault();
        idParent = childDTO.getIdParent();
    }

    public ChildEntity(ChildDTO childDTO, ObjectId idPar) {
        codiceFiscale = childDTO.getCodiceFiscale();
        name = childDTO.getName();
        surname = childDTO.getSurname();
        idFermataDefault = childDTO.getIdFermataDefault();
        idParent = idPar;
    }

    public ChildEntity(String cf, String name, String sur) {
        codiceFiscale = cf;
        this.name = name;
        surname = sur;
    }

}


