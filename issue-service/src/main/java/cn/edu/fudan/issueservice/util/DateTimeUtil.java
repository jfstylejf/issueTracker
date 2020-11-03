package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.ResponseBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static DateTimeFormatter Y_M_D_H_M_S_formatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("-")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)//第二个参数是宽度，比如2月份，如果宽度定为2，那么格式化后就是02
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
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)//第二个参数是宽度，比如2月份，如果宽度定为2，那么格式化后就是02
            .appendLiteral("-")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter();

    public static LocalDate parse(String dateStr){
        return LocalDate.parse(dateStr,Y_M_D_formatter);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(Y_M_D_H_M_S_formatter);
    }

    public static String format(Date date){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String y_m_d_format(LocalDateTime dateTime){
        return dateTime.format(Y_M_D_formatter);
    }

    public static String y_m_d_format(LocalDate dateTime){
        return dateTime.format(Y_M_D_formatter);
    }

    public static String timeTotimeStamp(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        //除以1000是将毫秒转成秒
        String res = String.valueOf(ts/1000);
        return res;
    }

    public static LocalDate dateToLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate stringToLocalDate(String date){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, fmt);
        return localDate;
    }

    public static Date stringToDate(String date){
        Date result = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Date localToUTC(String localTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date localDate= null;

        try {

            localDate = sdf.parse(localTime);

        } catch (ParseException e) {

            e.printStackTrace();

        }

        long localTimeInMillis=localDate.getTime();

        /** long时间转换成Calendar */

        Calendar calendar= Calendar.getInstance();

        calendar.setTimeInMillis(localTimeInMillis);

        /** 取得时间偏移量 */

        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);

        /** 取得夏令时差 */

        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);

        /** 从本地时间里扣除这些差量，即可以取得UTC时间*/

        calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        /** 取得的时间就是UTC标准时间 */

        Date utcDate=new Date(calendar.getTimeInMillis());

        return utcDate;

    }

    public static String UTCTimeToBeijingTime(String UTCTimeString){
        LocalDateTime date = stringToLocalDateTime(UTCTimeString);
        date = date.plusHours (8);
        return date.format (dateTimeFormatter);
    }

    public  static LocalDateTime stringToLocalDateTime(String dateString){
        return LocalDateTime.parse(dateString,dateTimeFormatter);
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
