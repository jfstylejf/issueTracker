package cn.edu.fudan.cloneservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;

import static org.reflections.Reflections.log;

public class DateTimeUtil {
    public static DateTimeFormatter GMSFormatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

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

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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

    public static String format(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static Date formatedDate(Date date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            return simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date transfer(String str) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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
            beginTime = beginTime.with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue());
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
}
