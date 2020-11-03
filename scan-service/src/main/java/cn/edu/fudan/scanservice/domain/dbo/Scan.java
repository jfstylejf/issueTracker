package cn.edu.fudan.scanservice.domain.dbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scan {


    public static final int INVOKE_SUCCESS = 1;
    public static final int INVOKE_FAILED = 0;

    private String uuid;
    private String startCommit;
    private String repoId;
    private String invokeResult;


    /**
     * 返回的key 为 工具表的工具id ，value 0表示调用失败，value 1 表示调用成功
     * @return
     */
    public Map<Integer,Integer> analyzeInvokeResult(){
        Map<Integer,Integer> toolInvokeMap = new HashMap<> (8);
        String[] toolInvokeResults = invokeResult.split (",");
        for(String toolInvokeResult : toolInvokeResults){
            String[] toolInvokeResultKeyAndValue = toolInvokeResult.split (":");
            String tool = toolInvokeResultKeyAndValue[0];
            String result = toolInvokeResultKeyAndValue[1];
            toolInvokeMap.put (Integer.parseInt (tool),Integer.parseInt (result));
        }

        return toolInvokeMap;
    }


}
