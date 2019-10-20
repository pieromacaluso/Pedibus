package it.polito.ai.mmap.pedibus.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "notificack")
public class NotificaAckEntity {
    @Id
    private ObjectId idNotifica;
    private ObjectId dispID;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Boolean isAck;
}
