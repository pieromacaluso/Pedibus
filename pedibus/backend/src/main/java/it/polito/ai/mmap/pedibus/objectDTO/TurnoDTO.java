package it.polito.ai.mmap.pedibus.objectDTO;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import it.polito.ai.mmap.pedibus.services.MongoTimeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TurnoDTO {
    private String idLinea;

    @JsonDeserialize(using = CustomDateDeserializer.class)
//    @JsonSerialize(using = CustomDateSerializer.class)
    private Date data;
    private Boolean verso;


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



//  Serve se restituiamo un TurnoDTO
//    static class CustomDateSerializer extends StdSerializer<Date> {
//
//
//        protected CustomDateSerializer() {
//            super(Date.class);
//        }
//
//        @Override
//        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//            jsonGenerator.writeString(MongoZonedDateTime.todo);
//        }
//    }


}
