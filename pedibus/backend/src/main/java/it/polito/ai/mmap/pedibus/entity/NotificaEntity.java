package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.management.Notification;
import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "notifica")
public class NotificaEntity {

    public enum NotificationType {
        BASE, DISPONIBILITA
    }

    @Id
    private String idNotifica;
    private NotificationType type;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Date data;


    //Per type = notifiche.type.Disponibilita, altrimenti impostati a null e false
    private ObjectId dispID;
    private Boolean isAck;

    public NotificaEntity(NotificationType type, String user, String msg, ObjectId dispID) {
        //idNotifica= new ObjectId().toString();
        this.usernameDestinatario = user;
        this.msg = msg;
        this.isTouched=false;
        if (type == NotificationType.BASE) {
            if (dispID!=null) {
                throw new NotificaWrongTypeException();             //Specificato type base ma anche un dispID
            }
            this.type = NotificationType.BASE;
            this.dispID = null;
        } else if (type == NotificationType.DISPONIBILITA) {
            this.type = NotificationType.DISPONIBILITA;
            this.dispID = dispID;
        }
        this.data = new Date();
    }
}
