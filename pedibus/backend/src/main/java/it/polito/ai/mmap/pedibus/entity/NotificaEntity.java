package it.polito.ai.mmap.pedibus.entity;

import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "notifica")
public class NotificaEntity {

    @Value("${notifiche.type.Base}")
    String NotBASE;
    @Value("${notifiche.type.Disponibilita}")
    String NotDISPONIBILITA;


    @Id
    private String idNotifica;
    private String Type;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;

    //Per type = notifiche.type.Disponibilita, altrimenti impostati a null e false
    private ObjectId dispID;
    private Boolean isAck;

    public NotificaEntity(String type,String user,String msg,Boolean isTouched,ObjectId dispID, Boolean isAck){
        idNotifica= new ObjectId().toString();
        this.usernameDestinatario=user;
        this.msg=msg;
        this.isTouched=isTouched;

        if(type.compareTo(NotBASE)==0){
            if(dispID.compareTo(null)!=0){
                throw new NotificaWrongTypeException();             //Specificato type base ma anche un dispID
            }
            this.dispID=null;
            this.isAck=false;
        }else if(type.compareTo(NotDISPONIBILITA)==0){
            this.dispID=dispID;
            this.isAck=isAck;
        }

    }

    public NotificaEntity(String type,String user,String msg,Boolean isTouched) {
        if(type.compareTo(NotBASE)==0){
            idNotifica= new ObjectId().toString();
            this.usernameDestinatario=user;
            this.msg=msg;
            this.isTouched=isTouched;
            this.dispID=null;
            this.isAck=false;
        }else{
            throw new NotificaWrongTypeException();
        }
    }

}
