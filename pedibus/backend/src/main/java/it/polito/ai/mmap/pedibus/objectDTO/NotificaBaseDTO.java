package it.polito.ai.mmap.pedibus.objectDTO;

import it.polito.ai.mmap.pedibus.entity.NotificaBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaBaseDTO {

    private String idNotifica;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;

    public NotificaBaseDTO(NotificaBaseEntity notificaBaseEntity){
        this.idNotifica=notificaBaseEntity.getIdNotifica();
        this.usernameDestinatario=notificaBaseEntity.getUsernameDestinatario();
        this.msg=notificaBaseEntity.getMsg();
        this.isTouched=notificaBaseEntity.getIsTouched();
    }


}
