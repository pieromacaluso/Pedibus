package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.NotificaAckEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaAckDTO {

    private String idNotifica;
    private ObjectId dispID;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Boolean isAck;

    public NotificaAckDTO(NotificaAckEntity notificaAckEntity){
        this.idNotifica=notificaAckEntity.getIdNotifica();
        this.dispID=notificaAckEntity.getDispID();
        this.usernameDestinatario=notificaAckEntity.getUsernameDestinatario();
        this.msg=notificaAckEntity.getMsg();
        this.isTouched=notificaAckEntity.getIsTouched();
        this.isAck=notificaAckEntity.getIsAck();
    }
}
