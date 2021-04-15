package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloneGroupSum {
    private String projectName;
    private String projectId;
    private String date;
    private int num;
}
