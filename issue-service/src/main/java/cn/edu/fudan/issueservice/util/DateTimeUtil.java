package cn.edu.fudan.issueservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class DateTimeUtil {

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter Y_M_D_H_M_S_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("-")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral("-")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(" ")
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(":")
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(":")
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();

    public static DateTimeFormatter Y_M_D_formatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("-")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral("-")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter();

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(Y_M_D_H_M_S_FORMATTER);
    }

    public static String format(Date date){
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static LocalDate stringToLocalDate(String date){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, fmt);
    }

    public static Date stringToDate(String date){
        Date result = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Date localToUtc(String localTime) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        Date localDate= null;

        try {

            localDate = sdf.parse(localTime);

        } catch (ParseException e) {

            e.printStackTrace();

        }

        assert localDate != null;
        long localTimeInMillis=localDate.getTime();

        Calendar calendar= Calendar.getInstance();

        calendar.setTimeInMillis(localTimeInMillis);


        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);

        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);

        calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        return new Date(calendar.getTimeInMillis());
    }

    public static String timeFormatIsLegal(String time, boolean isUntil){
        if(time == null){
            return null;
        }

        if(!time.matches("([0-9]+)-([0-9]{2})-([0-9]{2})")){
            return "time format error";
        }

        if(isUntil){
            time = DateTimeUtil.stringToLocalDate(time).plusDays(1).toString();
        }

        return time;
    }
}
