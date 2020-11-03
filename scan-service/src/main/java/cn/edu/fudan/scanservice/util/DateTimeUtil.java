package cn.edu.fudan.scanservice.util;

import cn.edu.fudan.scanservice.component.rest.RestInterfaceManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
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
import java.util.Locale;

/**
 * @author fancying
 */
@Slf4j
public class DateTimeUtil {


    public static DateTimeFormatter GMSFormatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
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

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String timeTotimeStamp(String s)  {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try{
            date  = simpleDateFormat.parse(s);
        }catch(ParseException e){
            e.printStackTrace();
        }

        long ts = date.getTime();
        //除以1000是将毫秒转成秒
        String res = String.valueOf(ts/1000);
        return res;
    }

    public static String format(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }


    public static String format(LocalDateTime date) {

        return date.format(dateTimeFormatter);
    }

    public  static LocalDateTime stringToLocalDate(String dateString){
        try {
            return LocalDateTime.parse(dateString, dateTimeFormatter);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return LocalDateTime.now();
    }

    public static String UTCTimeToBeijingTime(String UTCTimeString){
        LocalDateTime date = stringToLocalDate(UTCTimeString);
        return date.plusHours(8).toString().replace('T',' ');
    }



    /**
     * 10位时间戳转Date
     * @param time
     * @return
     */
    public static Date timestampToDate(Integer time) {
        long temp = (long) time * 1000;
        Timestamp ts = new Timestamp(temp);
        Date date = new Date();
        try {
            date = ts;
            //System.out.println(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;

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

    public static LocalDate dateToLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localToUTC(Date localDate) {


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

    public static String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(dateTimeFormatter);
    }


    public static void main(String[] args){
        String sD = "2020-08-03 13:07:19";
        LocalDateTime date = stringToLocalDate(sD);
        System.out.println(date.plusHours(8).toString().replace('T',' '));
    }


}
