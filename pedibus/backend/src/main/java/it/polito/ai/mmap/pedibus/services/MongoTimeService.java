package it.polito.ai.mmap.pedibus.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.mmap.pedibus.exception.SchoolClosedException;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class MongoTimeService {

    @Autowired
    ObjectMapper objectMapper;
    @Value("${dataInizioScuola}")
    private String dataInzioScuola;
    @Value("${dataFineScuola}")
    private String dataFineScuola;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<String> holidayList;

    private static Date parseData(String completeData) {
        ZonedDateTime londonTime = ZonedDateTime.parse(completeData, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS z"));
        return Date.from(londonTime.toInstant());
    }

    public static String dateToString(Date date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String localDateToString(LocalDate date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static Date getMongoZonedDateTimeFromDateTimeString(String date, String time) {

        return parseData(date + "T" + time + ":00.001 GMT+00:00");
    }

    public static Date getMongoZonedDateTimeFromTime(String time) {

        return parseData(LocalDate.now() + "T" + time + ":00.001 GMT+00:00");
    }

    public static Date getMongoZonedDateTimeFromDateTime(Date date, String time) {
        String strDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(date);
        return parseData(strDate.substring(0, 10) + "T" + time + ":00.001 GMT+00:00");
    }

    public static Date getNow() {
        return new Date();
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

    /**
     * Generazione di una lista di date sottoforma di stringa che inizia dalla data specificata e si conclude con la data
     * di fine scuola.
     *
     * @param dateTime data di inizio
     * @return lista di date
     */
    public List<String> generateListOfDateStartingFrom(LocalDate dateTime) {
        LocalDate iDate = LocalDate.from(dateTime);
        LocalDate fineScuolaPlusOne = LocalDate.parse(dataFineScuola).plusDays(1);
        List<String> listDate = new ArrayList<>();
        String dataString;
        for (; !iDate.equals(fineScuolaPlusOne); iDate = iDate.plusDays(1)) {
            try {
                isValidDate(iDate);
                listDate.add(iDate.toString());
            } catch (SchoolClosedException ignored) {
                // Ignora questa data, non verrà inserita nella lista
            }
        }

        return listDate;
    }

    @PostConstruct
    public void init() throws IOException {
        holidayList = objectMapper.readValue(ResourceUtils.getFile("classpath:building_data/holiday_date.json"), new TypeReference<List<String>>() {
        });
    }

    /**
     * Da String a a Date. Un booleano permette di controllare che la data sia valida per prenotazioni o turni
     *
     * @param date          Stringa nel formato 'yyyy-MM-dd' da convertire
     * @param checkValidity booleano che abilita il controllo
     * @return Date ottenuta
     */
    public Date getMongoZonedDateTimeFromDate(String date, boolean checkValidity) {
        return getDateCheckConstraints(date, checkValidity);
    }

    //non mettere dentro getMongoZonedDateTimeFromDate()
    //Se si cambia tipo di eccezione bisogna cambiare un paio di catch

    /**
     * Controllo dei vincoli per la data
     *
     * @param date          data da controllare nel formato 'yyyy-MM-hh'
     * @param checkValidity booleano per attivare o meno i controlli
     * @return Date ottenuta
     */
    public Date getDateCheckConstraints(String date, boolean checkValidity) {
        // holiday
        if (holidayList.contains(date) && checkValidity)
            throw new SchoolClosedException("Holiday");

        // weekend
        Date result = parseData(date + "T" + LocalTime.now().withHour(12).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if ((dayOfWeek == 7 || dayOfWeek == 1) && checkValidity)
            throw new SchoolClosedException("Weekend");

        // school closed
        Date schoolStart = parseData(dataInzioScuola + "T" + LocalTime.now().withHour(0).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        Date schoolEnd = parseData(dataFineScuola + "T" + LocalTime.now().withHour(23).withMinute(30).withSecond(30).withNano(500000000).toString() + " GMT+00:00");
        if ((result.before(schoolStart) || result.after(schoolEnd)) && checkValidity) {
            throw new SchoolClosedException("School closed");
        }
        return result;
    }

    public String isValidDate(LocalDate date) {
        String day = Integer.toString(date.getDayOfMonth());
        if (day.length() == 1)
            day = "0" + day;

        String month = Integer.toString(date.getMonth().getValue());
        if (month.length() == 1)
            month = "0" + month;
        getDateCheckConstraints(date.getYear() + "-" + month + "-" + day, true);
        return date.getYear() + "-" + month + "-" + day;
    }


    public String getOneValidDate(int shifAmount) {
        for (int i = 2; i < 120; i++) {
            try {
                return isValidDate(LocalDate.now().plus(i + shifAmount, ChronoUnit.DAYS));
            } catch (IllegalArgumentException ignored) {
            }
        }
        //restituisce null se con shiftAmount sforiamo la schoolEnd
        return null;
    }
}
