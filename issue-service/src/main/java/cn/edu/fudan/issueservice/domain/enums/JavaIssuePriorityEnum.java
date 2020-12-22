package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
public enum JavaIssuePriorityEnum {

    /**
     * 缺陷优先级
     */
    LOW("Low", 4),
    URGENT("Urgent", 1),
    NORMAL("Normal", 3),
    HIGH("High", 2),
    IMMEDIATE("Immediate", 0);

    private final String name;
    private final int rank;
    JavaIssuePriorityEnum(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    public static JavaIssuePriorityEnum getPriorityEnum(String name){
        for(JavaIssuePriorityEnum priority : JavaIssuePriorityEnum.values()){
            if(priority.getName().equals(name)){
                return priority;
            }
        }
        return null;
    }

    public static JavaIssuePriorityEnum getPriorityEnumByRank(int rank){
        for(JavaIssuePriorityEnum priority : JavaIssuePriorityEnum.values()){
            if(priority.getRank () == rank){
                return priority;
            }
        }
        return null;
    }
}
