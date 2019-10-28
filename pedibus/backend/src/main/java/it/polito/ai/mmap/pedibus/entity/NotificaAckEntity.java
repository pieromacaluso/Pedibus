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
    private String idNotifica;
    private ObjectId dispID;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Boolean isAck;

    public NotificaAckEntity(ObjectId dispID,String user,String msg,Boolean isTouched, Boolean isAck){
        idNotifica= new ObjectId().toString();
        this.dispID=dispID;
        this.usernameDestinatario=user;
        this.msg=msg;
        this.isTouched=isTouched;
        this.isAck=isAck;
    }
}
