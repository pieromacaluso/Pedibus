package it.polito.ai.mmap.pedibus.configuration;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Data
public class MongoZonedDateTime extends Date {

    private static Date parseData(String completeData) {
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS z"));
        return Date.from(londonTime.toInstant());
    }

    public static Date getMongoZonedDateTimeFromDate(String date) {

        return parseData(date + "T" + LocalTime.now().withHour(12).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
    }

    public static Date getMongoZonedDateTimeFromTime(String time) {

        return parseData(LocalDate.now() + "T" + time + ":00.001 GMT+00:00");
    }

    public static Date getMongoZonedDateTimeFromDateTime(Date date, String time) {
        String strDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(date);
        return parseData(strDate.substring(0, 10) + "T" + time + ":00.001 GMT+00:00");
    }

    public static Date getNow() {
        return parseData(LocalDateTime.now().withNano(1000000).toString() + " GMT+00:00");
    }


    public static Date getStartOfToday() {
        return parseData(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(1000000).toString() + " GMT+00:00");
    }

    public static Date getStartOfTomorrow() {
        return parseData(LocalDateTime.now().plus(1, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(1000000).toString() + " GMT+00:00");
    }

    public static boolean isToday(Date data) {
        return data.after(getStartOfToday()) && data.before(getStartOfTomorrow());
    }
}
