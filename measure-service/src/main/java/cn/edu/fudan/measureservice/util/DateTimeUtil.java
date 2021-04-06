package cn.edu.fudan.measureservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class DateTimeUtil {

    private static DateTimeFormatter Y_M_D_H_M_S_formatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("-")
            //第二个参数是宽度，比如2月份，如果宽度定为2，那么格式化后就是02
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
            //第二个参数是宽度，比如2月份，如果宽度定为2，那么格式化后就是02
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral("-")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter();

    public static DateTimeFormatter Y_M_formatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("-")
            //第二个参数是宽度，比如2月份，如果宽度定为2，那么格式化后就是02
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
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

    public static String y_m_format(LocalDate dateTime){
        return dateTime.format(Y_M_formatter);
    }

    public static String timeTotimeStamp(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        //除以1000是将毫秒转成秒
        String res = String.valueOf(ts/1000);
        return res;
    }

    public static Date transfer(LocalDateTime target){
        try{
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(format(target));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static boolean isTheSameDay(LocalDate a,LocalDate b){
        if(a.getYear()==b.getYear() && a.getMonthValue()==b.getMonthValue() && a.getDayOfMonth()==b.getDayOfMonth()){
            return true;
        }
        return false;
    }

    public static LocalDate stringToLocalDate(String date){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, fmt);
        return localDate;
    }
}
