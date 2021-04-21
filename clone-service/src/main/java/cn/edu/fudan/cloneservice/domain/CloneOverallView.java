package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class CloneOverallView {
    private String projectName;
    private String projectId;
    private String date;
    private String repository;
    private int caseSum;
    private int fileSum;
    private int codeLengthAverage;
    private int cloneType;
}
