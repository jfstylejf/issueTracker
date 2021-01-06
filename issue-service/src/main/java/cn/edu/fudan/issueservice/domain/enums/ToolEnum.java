package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToolEnum {
    //findbugs 工具
    FINDBUGS("findbugs"),
    //sonar qube 工具
    SONAR("sonarqube"),
    //ESLint 工具
    ESLINT("ESLint");
    private final String type;
}
