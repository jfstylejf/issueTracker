package cn.edu.fudan.measureservice.util;

import cn.edu.fudan.measureservice.domain.enums.GranularityEnum;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
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

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        return LocalDate.parse(date, fmt);
    }

    /**
     * 根据间隔初始化时间
     * case : {@link GranularityEnum}
     * 若按周间隔，则起始时间为周一
     * 若按月间隔，则起始时间为这个月的第一天
     * 若按年间隔，则起始时间为这一年的第一天
     * @param beginTime 起始时间
     * @param interval
     * @return
     */
    public static LocalDate initBeginTimeByInterval(LocalDate beginTime, String interval) {
        if (interval.equals(GranularityEnum.Day.getType())) {
           return beginTime;
        }else if(interval.equals(GranularityEnum.Week.getType())) {
            beginTime = beginTime.with(ChronoField.DAY_OF_WEEK,DayOfWeek.MONDAY.getValue());
            return beginTime;
        }else if(interval.equals(GranularityEnum.Month.getType())) {
            beginTime = beginTime.with(TemporalAdjusters.firstDayOfMonth());
            return beginTime;
        }else if(interval.equals(GranularityEnum.Year.getType())) {
            beginTime = beginTime.with(TemporalAdjusters.firstDayOfYear());
            return beginTime;
        }else {
            log.error("wrong given interval\n");
            return null;
        }
    }

    /**
     * 根据间隔初始化时间
     * case : {@link GranularityEnum}
     * 若按周间隔，则结束时间为周日
     * 若按月间隔，则结束时间为这个月的月末
     * 若按年间隔，则结束时间为这一年的最后一天
     * @param endTime 结束时间
     * @param interval
     * @return
     */
    public static LocalDate initEndTimeByInterval(LocalDate endTime, String interval) {
        if (interval.equals(GranularityEnum.Day.getType())) {
            return endTime;
        }else if(interval.equals(GranularityEnum.Week.getType())) {
            endTime = endTime.with(ChronoField.DAY_OF_WEEK,DayOfWeek.SUNDAY.getValue());
            return endTime;
        }else if(interval.equals(GranularityEnum.Month.getType())) {
            endTime = endTime.with(TemporalAdjusters.lastDayOfMonth());
            return endTime;
        }else if(interval.equals(GranularityEnum.Year.getType())) {
            endTime = endTime.with(TemporalAdjusters.lastDayOfYear());
            return endTime;
        }else {
            log.error("wrong given interval\n");
            return null;
        }
    }


    public static LocalDate selectTimeIncrementByInterval(LocalDate beginTime, String interval) {
        if (interval.equals(GranularityEnum.Day.getType())) {
            beginTime = beginTime.plusDays(1);
            return beginTime;
        }else if(interval.equals(GranularityEnum.Week.getType())) {
            beginTime = beginTime.plusWeeks(1);
            return beginTime;
        }else if(interval.equals(GranularityEnum.Month.getType())) {
            beginTime = beginTime.plusMonths(1);
            return beginTime;
        }else if(interval.equals(GranularityEnum.Year.getType())) {
            beginTime = beginTime.plusYears(1);
            return beginTime;
        }else {
            log.error("wrong given interval\n");
            return null;
        }
    }

    /**
     * 查询时间统一处理加一天
     * @param until 查询截止时间
     * @return String until
     */
    public static String processUntil(String until) {
        try {
            if(until!=null && !"".equals(until)) {
                until = DateTimeUtil.dtf.format(LocalDate.parse(until,DateTimeUtil.dtf).plusDays(1));
            }else {
                until = DateTimeUtil.dtf.format(LocalDate.now().plusDays(1));
            }
            return until;
        }catch (Exception e) {
            e.getMessage();
        }
        return null;
    }


}
