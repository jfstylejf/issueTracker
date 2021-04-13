package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LocationInfo {
    int beginLine;
    int endLine;
    String className;
    String methodName;

}
