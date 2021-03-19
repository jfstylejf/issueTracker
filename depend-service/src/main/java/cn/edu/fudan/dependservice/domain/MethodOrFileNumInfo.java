package cn.edu.fudan.dependservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class MethodOrFileNumInfo implements Serializable {
    private String projectName;
    private String projectId;
    private String date;
    private int num;
    private List<MethodOrFileDetailInfo> detail;

    public MethodOrFileNumInfo() {

    }

}
