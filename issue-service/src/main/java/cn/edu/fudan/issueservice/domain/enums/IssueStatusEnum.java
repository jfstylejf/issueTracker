package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum IssueStatusEnum {

    /**
     * IGNORE 代表不解决该缺陷
     * OPEN   代表该缺陷正待解决
     * MISINFORMATION   代表该缺陷属于误报，假阳性
     * SOLVED  代表该缺陷已被解决
     * TO_REVIEW  代表该问题需要review是否真的是个缺陷
     */
    IGNORE("Ignore"),
    OPEN("Open"),
    MISINFORMATION("Misinformation"),
    SOLVED("Solved"),
    TO_REVIEW("To_Review");

    private String name;
    IssueStatusEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static IssueStatusEnum getStatusByName(String name){
        for(IssueStatusEnum status : IssueStatusEnum.values()){
            if(status.getName().equals(name)){
                return status;
            }
        }
        return null;
    }
}
