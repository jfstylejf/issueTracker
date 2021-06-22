package cn.edu.fudan.taskmanagement.domain.taskinfo;


/**
 * @author zyh
 * @date 2020/7/8
 */

public class Status {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(TaskStatusList.toDo.contains(name)){
            this.name = "toDo";
        }
        else if(TaskStatusList.inProgress.contains(name)){
            this.name = "inProgress";
        }
        else if(TaskStatusList.done.contains(name)){
            this.name = "done";
        }
        else if (TaskStatusList.reopen.contains(name)){
            this.name = "reOpen";
        }
        else {
            this.name = "undefined";
        }
    }
}
