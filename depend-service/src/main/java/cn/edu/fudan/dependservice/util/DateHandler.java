package cn.edu.fudan.dependservice.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Song Rui
 * description: 日期/时间处理
 */
public class DateHandler {

    /**
     * 如果传入日期不存在，则获取1990-01-01至今的日期范围
     * @return {beginDate, endDate}
     */
    public static List<String> handleParamDate(String beginDate, String endDate){
        List<String> dates= new ArrayList<>(2);
        if(beginDate == null || beginDate.length() == 0){
            beginDate= "1990-01-01";
        }
        if(endDate == null || endDate.length() == 0){
            Calendar calendar = Calendar.getInstance();
            endDate= calendar.get(Calendar.YEAR)+ "-"+ (calendar.get(Calendar.MONTH)+ 1)+ "-" + calendar.get(Calendar.DATE);
        }
        dates.add(beginDate+ " 00:00:00");
        dates.add(endDate + " 24:00:00");
        return dates;
    }

}
