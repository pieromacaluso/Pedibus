package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.objectDTO.NotificaDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "notifica")
public class NotificaEntity {

    //todo non funziona nei costruttori, vedere se possibile trovare una soluzione cosi da aver le stringhe solo nelle application properties
    /*@Value("${notifiche.type.Base}")
    private String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    private String NotDISPONIBILITA;*/

    String NotBASE = "base";
    String NotDISPONIBILITA = "disponibilita";


    @Id
    private String idNotifica;
    private String type;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;

    //Per type = notifiche.type.Disponibilita, altrimenti impostati a null e false
    private ObjectId dispID;
    private Boolean isAck;

    public NotificaEntity(String type, String user, String msg, ObjectId dispID) {
        //idNotifica= new ObjectId().toString();
        this.usernameDestinatario = user;
        this.msg = msg;

        if (type.compareTo(NotBASE) == 0) {
            if (dispID!=null) {
                throw new NotificaWrongTypeException();             //Specificato type base ma anche un dispID
            }
            this.type = NotBASE;
            this.dispID = null;
        } else if (type.compareTo(NotDISPONIBILITA) == 0) {
            this.type = NotDISPONIBILITA;
            this.dispID = dispID;
        }

    }


//TODO delete

//   public NotificaEntity(NotificaDTO notificaDTO) {
//        if (notificaDTO.getType().compareTo(NotBASE) == 0) {
//            if (notificaDTO.getDispID() != null)
//                throw new NotificaWrongTypeException();
//            //idNotifica=notificaDTO.getIdNotifica();
//            this.type = NotBASE;
//            usernameDestinatario = notificaDTO.getUsernameDestinatario();
//            msg = notificaDTO.getMsg();
//            isTouched = notificaDTO.getIsTouched();
//            dispID = null;
//            isAck = false;
//
//        } else if (notificaDTO.getType().compareTo(NotDISPONIBILITA) == 0) {
//            if (notificaDTO.getDispID() == null)
//                throw new NotificaWrongTypeException();
//            //idNotifica=notificaDTO.getIdNotifica();
//            this.type = NotDISPONIBILITA;
//            usernameDestinatario = notificaDTO.getUsernameDestinatario();
//            msg = notificaDTO.getMsg();
//            isTouched = notificaDTO.getIsTouched();
//            dispID = notificaDTO.getDispID();
//            isAck = notificaDTO.getIsAck();
//        } else {
//            throw new NotificaWrongTypeException();
//        }
//    }

}
