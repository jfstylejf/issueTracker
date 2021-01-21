package cn.edu.fudan.issueservice.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

/**
 * @author Beethoven
 */
public class SegmentationUtil {

    public static void splitString(String[] queryName, String[] splitStrings, Map<String, Object> query) {
        for(int i = 0;i< splitStrings.length;i++){
            if(!StringUtils.isEmpty(splitStrings[i])){
                query.put(queryName[i], splitStringList(splitStrings[i]));
            }else{
                query.put(queryName[i], null);
            }
        }
    }

    public static List<String> splitStringList(String splitString){
        if(StringUtils.isEmpty(splitString)){
            return new ArrayList<>();
        }
        return Arrays.asList(splitString.split(","));
    }

    public static String unionStringList(List<String> list){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if(i != 0){
                result.append(",");
            }
            result.append(list.get(i));
        }
        return result.toString();
    }
}
