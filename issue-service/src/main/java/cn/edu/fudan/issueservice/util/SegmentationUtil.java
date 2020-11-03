package cn.edu.fudan.issueservice.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class SegmentationUtil {

    public static Map<String, Object> splitString(String[] queryName,String[] splitStrings, Map<String, Object> query) {

        for(int i = 0;i< splitStrings.length;i++){
            if(!StringUtils.isEmpty(splitStrings[i])){
                query.put(queryName[i], splitStringList(splitStrings[i]));
            }else{
                query.put(queryName[i], null);
            }
        }

        return query;
    }

    public static List<String> splitStringList(String splitString){
        if(StringUtils.isEmpty(splitString)){
            return null;
        }
        return Arrays.asList(splitString.split(","));
    }
}
