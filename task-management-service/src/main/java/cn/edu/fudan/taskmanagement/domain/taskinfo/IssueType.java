package cn.edu.fudan.taskmanagement.domain.taskinfo;

/**
 * @author zyh
 * @date 2020/7/3
 */

public class IssueType {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(IssueTypeList.bug.contains(name)){
            this.name = "bug";
        }
        else if(IssueTypeList.task.contains(name)){
            this.name = "task";
        }
        else {
            this.name = "others";
        }
    }

}
