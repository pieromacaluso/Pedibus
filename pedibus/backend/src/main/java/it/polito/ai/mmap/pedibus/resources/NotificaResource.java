package it.polito.ai.mmap.pedibus.resources;


import lombok.Data;
import org.bson.types.ObjectId;

/**
 * Resource usata per inviare la notifica al frontend senza inviare l'intero oggetto NotificaEntity
 */
@Data
public class NotificaResource {
    public String idNotifica;
    public String msg;

    public NotificaResource(String idNotifica, String msg){
        this.idNotifica=idNotifica;
        this.msg=msg;
    }
}
