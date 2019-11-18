package cn.edu.fudan.issueservice.util;

public class SearchUtil {

//    public static <T extends Comparable> int dichotomy(T[] array,){
//
//    }

    public static  int dichotomy(String[] strings, String value){
        int middle;
        int start =0;
        int end = strings.length-1;
        while (start <= end) {
            if(start == end ){
                return -1;
            }else{
                middle = (start+end)/2;
                int compareResult = value.compareTo(strings[middle]);
                if(compareResult==0){
                    return middle;
                }else if(compareResult==-1){
                    end = --middle;
                }else{
                    start=++middle;
                }
            }
        }
        return -1;
    }
}