package cn.edu.fudan.taskmanagement.util;

import org.springframework.stereotype.Component;

@Component
public class StringUtil {

    public StringUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
