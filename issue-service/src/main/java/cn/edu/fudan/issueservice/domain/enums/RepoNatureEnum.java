package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepoNatureEnum {
    /**
     *
     */
    MAIN("main"),
    UPDATE("update");

    private String type;
}
