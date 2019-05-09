package it.polito.ai.mmap.esercitazione3.entity;

import it.polito.ai.mmap.esercitazione3.configuration.MongoZonedDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;

/**
 * Classe che serve a memorizzare temporaneamente il
 * token utilizzato per la recovery della password.
 * Questa entity va creata in fase di recovery per
 * poi essere eliminata una volta che la recovery viene effettuata.
 */

@Data
@Document(collection = "recoverTokens")
public class RecoverTokenEntity {

    @Id
    private String username;
    private String tokenValue;

    //Se si cambia il valore di expire bisogna fare il drop dell'indice su atlas
    @Indexed(name = "ttl_index", expireAfterSeconds = 60*1)
    Date creationDate;

    public RecoverTokenEntity() {
    }

    public RecoverTokenEntity(String email) {
        this.username = email;
        byte[] arr = new byte[10];
        new Random().nextBytes(arr);
        String randomValue = new String(arr, Charset.forName("UTF-8"));
        this.tokenValue = randomValue;

        creationDate = new MongoZonedDateTime().getNow();


    }

}
