package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.services.MongoZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

/**
 * Classe che serve a memorizzare temporaneamente il
 * token utilizzato per la recovery della password.
 * Questa entity va creata in fase di recovery per
 * poi essere eliminata una volta che la recovery viene effettuata.
 */

@Data
@NoArgsConstructor
@Document(collection = "recoverTokens")
public class RecoverTokenEntity {

    @Id
    private ObjectId id;
    private ObjectId userId;

    //Se si cambia il valore di expire bisogna fare il drop dell'indice su atlas
    // Scadenza dopo 1h
    @Indexed(name = "ttl_index", expireAfterSeconds = 1*3600)
    Date creationDate;

    public RecoverTokenEntity(ObjectId userId) {
        this.id = new ObjectId();
        this.userId = userId;
        ZonedDateTime londonTime = ZonedDateTime.now(ZoneId.of("UTC+02:00"));
        creationDate = Date.from(londonTime.toInstant());
    }

}
