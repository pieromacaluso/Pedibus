package it.polito.ai.mmap.pedibus.objectDTO;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import it.polito.ai.mmap.pedibus.entity.NotificaEntity;
import it.polito.ai.mmap.pedibus.exception.NotificaWrongTypeException;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificaDTO {

    private String idNotifica;
    private NotificaEntity.NotificationType type;
    private ObjectId dispID;
    private String usernameDestinatario;
    private String msg;
    private Boolean isTouched;
    private Boolean isAck;
    @JsonDeserialize(using = NotificaDTO.CustomDateDeserializer.class)
//    @JsonSerialize(using = CustomDateSerializer.class)
    private Date data;


    static class CustomDateDeserializer extends StdDeserializer<Date> {
        @Autowired
        MongoTimeService mongoTimeService;

        public CustomDateDeserializer() {
            this(null);
        }

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return mongoTimeService.getMongoZonedDateTimeFromDate(jsonParser.getText());
        }

        public CustomDateDeserializer(Class<?> vc) {
            super(vc);
        }

    }

    public NotificaDTO(NotificaEntity notificaEntity) {
        this.idNotifica = notificaEntity.getIdNotifica();
        this.type = notificaEntity.getType();
        this.dispID = notificaEntity.getDispID();
        this.usernameDestinatario = notificaEntity.getUsernameDestinatario();
        this.msg = notificaEntity.getMsg();
        this.isTouched = notificaEntity.getIsTouched();
        this.isAck = notificaEntity.getIsAck();
        this.data = notificaEntity.getData();

    }

    public NotificaDTO(NotificaEntity.NotificationType type, String user, String msg, Boolean isTouched) {
        if (type == NotificaEntity.NotificationType.BASE) {
            //idNotifica= new ObjectId().toString();
            this.type = NotificaEntity.NotificationType.BASE;
            this.usernameDestinatario = user;
            this.msg = msg;
            this.isTouched = isTouched;
            this.dispID = null;
            this.isAck = false;
            this.data = new Date();
        } else {
            throw new NotificaWrongTypeException();
        }
    }
}
