package cn.edu.fudan.dependservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class DependencyInfo implements Serializable {
    private String projectName;
    private String projectId;
    private String date;
    private Integer num;
    private List<DependencyDetailInfo> detail;

    public DependencyInfo() {

    }

}
