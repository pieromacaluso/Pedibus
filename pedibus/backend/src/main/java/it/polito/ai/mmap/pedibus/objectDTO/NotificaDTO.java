package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaDTO {

    String NotBASE="base";
    String NotDISPONIBILITA="disponibilita";

    private String idNotifica;
    private String type;
    private ObjectId dispID;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Boolean isAck;

    public NotificaDTO(NotificaEntity notificaEntity){
        this.idNotifica=notificaEntity.getIdNotifica();
        this.type=notificaEntity.getType();
        this.dispID=notificaEntity.getDispID();
        this.usernameDestinatario=notificaEntity.getUsernameDestinatario();
        this.msg=notificaEntity.getMsg();
        this.isTouched=notificaEntity.getIsTouched();
        this.isAck=notificaEntity.getIsAck();
    }
    public NotificaDTO(String type,String user,String msg,Boolean isTouched) {
        if(type.compareTo(NotBASE)==0){
            //idNotifica= new ObjectId().toString();
            this.type=NotBASE;
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
