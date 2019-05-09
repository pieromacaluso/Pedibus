package it.polito.ai.mmap.esercitazione3.configuration;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
public class MongoZonedDateTime extends Date {
    Date now;

    public MongoZonedDateTime()
    {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS z";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        String completeData = LocalDateTime.now().toString() + " GMT+00:00";
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, dateTimeFormatter);
        now = Date.from(londonTime.toInstant());
    }

    public Date getMongoZonedDateTimeFromDate(String date)
    {
        String completeData = date + " 12:00 GMT+00:00"; //data nel formato AAAA-MM-DD
        String pattern = "yyyy-MM-dd HH:mm z";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, dateTimeFormatter);
        return Date.from(londonTime.toInstant());
    }

}
