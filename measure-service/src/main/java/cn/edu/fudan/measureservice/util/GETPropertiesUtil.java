package cn.edu.fudan.measureservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName: GETPropertiesUtil
 * @Description:  解决数据注入问题 （对于new 出来的对象 Spring注入会失败）
 * @Author wjzho
 * @Date 2021/5/13
 */
//注意此时要加注解
@Component
public class GETPropertiesUtil {

    private static String jsResultFileHome;

    public static String getJsResultFileHome()
    {
        return jsResultFileHome;
    }
    @Value("${jsResultFileHome}")
    public void setJsResultFileHome(String jsResultFileHome)
    {
        GETPropertiesUtil.jsResultFileHome = jsResultFileHome;
    }
}
