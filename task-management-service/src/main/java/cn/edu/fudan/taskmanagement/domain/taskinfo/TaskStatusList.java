package cn.edu.fudan.taskmanagement.domain.taskinfo;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public interface TaskStatusList {
    List<String> toDo = Arrays.asList("OPEN","待办","开放");
    List<String> inProgress = Arrays.asList("开发中");
    List<String> done = Arrays.asList("已解决", "完成", "关闭");
    List<String> reopen = Arrays.asList("重新打开");
}
