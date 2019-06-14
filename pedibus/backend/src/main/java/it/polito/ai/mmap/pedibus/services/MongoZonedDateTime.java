package it.polito.ai.mmap.pedibus.services;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
public class MongoZonedDateTime extends Date {

    public static Date parseData(String pattern, String completeData) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, dateTimeFormatter);
        return Date.from(londonTime.toInstant());
    }


    public static Date getNow() {
        return parseData("yyyy-MM-dd'T'HH:mm:ss.SSS z", LocalDateTime.now().withNano(1000000).toString() + " GMT+00:00");
    }

    public static Date getMongoZonedDateTimeFromDate(String date)
    {

        return parseData("yyyy-MM-dd HH:mm z", date + " 12:00 GMT+00:00");
    }

}
