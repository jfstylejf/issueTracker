package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScanStatusEnum {
    /**
     *
     */
    DOING ("doing"),
    COMPILE_FAILED("compile failed"),
    INVOKE_TOOL_FAILED("invoke tool failed"),
    ANALYZE_FAILED("analyze failed"),
    PERSIST_FAILED("persist failed"),
    MATCH_FAILED("match failed"),
    STATISTICAL_FAILED("statistical failed"),
    DONE ("done");

    private String type;



    public static ScanStatusEnum getByType(String type){
        for(ScanStatusEnum scanStatusEnum : values()){
            if (scanStatusEnum.getType ().equals (type)) {
                return scanStatusEnum;
            }
        }
        return null;
    }

}
