package cn.edu.fudan.dependservice.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @version 1.0
 * @autthor shaoxi
 * @description transfer datetime to timestamp that jgit need(unit is 's',not 'ms' )
 */
public class TimeUtil {

    public static String getCurrentDateTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return  df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * @author shaoxi
     * @param:  2015-07-03 22:20:20
     * @return:  like: 1435852800  it's unit is 's' but not 'ms'
     **/
    public static int timeStampforJgit(String datetime) {
        Date d = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            d = sf.parse(datetime);// 日期转换为时间戳
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int)(d.getTime()/1000);
    }
    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        return  df.format(new Date());// new Date()为获取当前系统时间
    }
}
