package it.polito.ai.mmap.pedibus.objectDTO;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mongodb.Mongo;
import it.polito.ai.mmap.pedibus.configuration.MongoZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

        public CustomDateDeserializer() {
            this(null);
        }

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return MongoZonedDateTime.getMongoZonedDateTimeFromDate(jsonParser.getText());
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
