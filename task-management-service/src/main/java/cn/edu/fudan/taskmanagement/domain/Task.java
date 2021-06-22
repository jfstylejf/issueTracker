package cn.edu.fudan.taskmanagement.domain;

import cn.edu.fudan.taskmanagement.domain.taskinfo.Fields;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zyh
 * @date 2020/7/3
 */
@Getter
@Setter
public class Task {

    private String key;

    private Fields fields;

    public Fields getFields() {
        return fields;
    }
}
