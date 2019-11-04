package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaDTO {

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
}
