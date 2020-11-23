package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RawIssueStatus {
    //issue 的 第一条raw issue 插入时的状态
    ADD("add"),
    //raw issue 对比前一条raw issue location 发生改变
    CHANGED("changed"),
    //默认情况设置为default
    DEFAULT("default"),
    //这个issue 在这个commit 通过开发者修改代码被消除
    SOLVED("solved"),
    //这个issue 在这个commit 通过merge的方式自动消除
    MERGE_SOLVED("merge solved");
    private final String type;

    public static RawIssueStatus getStatusByName(String name){
        for(RawIssueStatus status : RawIssueStatus.values()){
            if(status.getType().equals(name)){
                return status;
            }
        }
        return null;
    }
}
