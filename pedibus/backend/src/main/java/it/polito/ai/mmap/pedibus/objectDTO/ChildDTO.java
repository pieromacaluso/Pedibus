package it.polito.ai.mmap.pedibus.objectDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.polito.ai.mmap.pedibus.entity.ChildEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.IOException;

@Data
@NoArgsConstructor
public class ChildDTO {

    private String codiceFiscale; //Se si cambia questo campo bisogna cambiare "$.alunniPerFermataAndata[0].alunni[0].codiceFiscale" in test2
    private String name;
    private String surname;

    //TODO resource o lasciamo queste due annotazioni ?
    @JsonIgnore
    private Integer idFermataDefault;   //in fase di registrazione ad ogni bambino bisogna indicare la sua fermata di default dalla quale partire/arrivare

    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId idParent;

    public ChildDTO(ChildEntity childEntity){
        codiceFiscale=childEntity.getCodiceFiscale();
        name=childEntity.getName();
        surname=childEntity.getSurname();
        idFermataDefault=childEntity.getIdFermataDefault();
        idParent=childEntity.getIdParent();
    }


    private static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(objectId.toString());
        }


    }
}


