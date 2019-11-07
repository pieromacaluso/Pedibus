package it.polito.ai.mmap.pedibus.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MongoTimeService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Value("${dataInizioScuola}")
    private String dataInzioScuola;
    @Value("${dataFineScuola}")
    private String dataFineScuola;

    private List<String> holidayList;

    @PostConstruct
    public void init() throws IOException {
        holidayList = objectMapper.readValue(ResourceUtils.getFile("classpath:building_data/holiday_date.json"), new TypeReference<List<String>>() {
        });
    }


    private static Date parseData(String completeData) {
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS z"));
        return Date.from(londonTime.toInstant());
    }

    public Date getMongoZonedDateTimeFromDate(String date) {
        return getDateCheckConstraints(date);
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

    //non mettere dentro getMongoZonedDateTimeFromDate()
    //Se si cambia tipo di eccezione bisogna cambiare un paio di catch
    public Date getDateCheckConstraints(String date) {
        // holiday
        if (holidayList.contains(date))
            throw new IllegalArgumentException("Holiday");

        // weekend
        Date result = parseData(date + "T" + LocalTime.now().withHour(12).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 7 || dayOfWeek == 1)
            throw new IllegalArgumentException("Weekend");

        // school closed
        Date schoolStart = parseData(dataInzioScuola + "T" + LocalTime.now().withHour(0).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        Date schoolEnd = parseData(dataFineScuola + "T" + LocalTime.now().withHour(23).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        if (result.before(schoolStart) || result.after(schoolEnd)) {
            throw new IllegalArgumentException("School closed");
        }
        return result;
    }

    public String dateCheckConstraint(LocalDate date) {
        String day = Integer.toString(date.getDayOfMonth());
        if (day.length() == 1)
            day = "0" + day;

        String month = Integer.toString(date.getMonth().getValue());
        if (month.length() == 1)
            month = "0" + month;
        getDateCheckConstraints(date.getYear() + "-" + month + "-" + day);
        return date.getYear() + "-" + month + "-" + day;
    }
}
