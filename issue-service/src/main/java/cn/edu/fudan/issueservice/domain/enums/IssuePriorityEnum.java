package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

@Getter
public enum IssuePriorityEnum {

    LOW("Low", 4),
    URGENT("Urgent", 1),
    NORMAL("Normal", 3),
    HIGH("High", 2),
    IMMEDIATE("Immediate", 0);

    private String name;
    private int rank;
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
