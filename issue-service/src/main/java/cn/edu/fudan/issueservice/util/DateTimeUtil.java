package cn.edu.fudan.issueservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
public class DateTimeUtil {

    private static final  String DATE_FORMAT_WITH_DETAIL = "yyyy-MM-dd HH:mm:ss";

    private static final  String DATE_FORMAT = "yyyy-MM-dd";

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
        return new SimpleDateFormat(DATE_FORMAT_WITH_DETAIL).format(date);
    }

    public static LocalDate stringToLocalDate(String date){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return LocalDate.parse(date, fmt);
    }

    public static Date stringToDate(String date){
        Date result = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_WITH_DETAIL);
        try {
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Date localToUtc(String localTime) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_WITH_DETAIL);

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

    public static List<String[]> getPeriodsByInterval(String since, String until, String interval) throws ParseException {

        List<String[]> periods = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(format.parse(since));
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(format.parse(until));

        List<String> dateList = new ArrayList<>();
        while (format.parse(until).after(calBegin.getTime())) {
            dateList.add(format.format(calBegin.getTime()));
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
        }
        dateList.add(format.format(calBegin.getTime()));

        switch (interval){
            case "day":
                dateList.forEach(date -> periods.add(new String[]{date, date}));
                break;
            case "week":
                getFirstDayAndLastDayInWeek(dateList, periods);
                break;
            case "month":
                getFirstDayAndLastDayInMonth(dateList, periods);
                break;
            case "year":
                getFirstDayAndLastDayInYear(dateList, periods);
                break;
            default:
        }

        return periods;
    }

    private static void getFirstDayAndLastDayInYear(List<String> dateList, List<String[]> periods) {

    }

    private static void getFirstDayAndLastDayInMonth(List<String> dateList, List<String[]> periods) {

    }

    private static void getFirstDayAndLastDayInWeek(List<String> dateList, List<String[]> periods) {

    }
}
