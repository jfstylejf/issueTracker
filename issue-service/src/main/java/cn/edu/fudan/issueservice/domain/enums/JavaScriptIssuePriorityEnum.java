package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
public enum JavaScriptIssuePriorityEnum {
    /**
     * JS缺陷优先级
     */
    OFF("Off", 0),
    WARN("Warn", 1),
    ERROR("Error", 2);

    private final String name;
    private final int rank;

    JavaScriptIssuePriorityEnum(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    public static String getPriorityByRank(int rank){
        for(JavaScriptIssuePriorityEnum priority : JavaScriptIssuePriorityEnum.values()){
            if(priority.getRank() == rank){
                return priority.name;
            }
        }
        return null;
    }
}