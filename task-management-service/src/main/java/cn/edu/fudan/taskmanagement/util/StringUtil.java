package cn.edu.fudan.taskmanagement.util;

import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StringUtil {
    @Autowired
    private JiraMapper jiraMapper;

    public String getJiraIdFromCommitMsg(String commitMsg) {

        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
        // 调用pattern对象的matcher方法，传入需要匹配的字符串， 得到一个匹配器对象
        Matcher matcher = pattern.matcher(commitMsg);

        // 从字符串开头，返回匹配到的第一个字符串
        if (matcher.find()) {
            // 输出第一次匹配的内容
            // log.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraUuid";
    }

    public StringUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
