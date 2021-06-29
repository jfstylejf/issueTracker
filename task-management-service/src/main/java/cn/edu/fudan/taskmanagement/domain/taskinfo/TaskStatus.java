package cn.edu.fudan.taskmanagement.domain.taskinfo;

/**
 * @author zyh
 * @date 2020/7/8
 *
 * 上汽任务类型：In Progress   To Do   Done
 */
public enum TaskStatus {
    //处理中
    InProgress("In Progress"),
    //待办
    ToDo("To Do"),
    //完成
    Done("Done");

    private String desc;

    private TaskStatus(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }
}
