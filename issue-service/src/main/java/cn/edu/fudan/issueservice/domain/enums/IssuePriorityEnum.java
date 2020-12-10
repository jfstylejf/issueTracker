package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
public enum IssuePriorityEnum {

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
    IssuePriorityEnum(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    public static IssuePriorityEnum getPriorityEnum(String name){
        for(IssuePriorityEnum priority : IssuePriorityEnum.values()){
            if(priority.getName().equals(name)){
                return priority;
            }
        }
        return null;
    }

    public static IssuePriorityEnum getPriorityEnumByRank(int rank){
        for(IssuePriorityEnum priority : IssuePriorityEnum.values()){
            if(priority.getRank () == rank){
                return priority;
            }
        }
        return null;
    }
}
