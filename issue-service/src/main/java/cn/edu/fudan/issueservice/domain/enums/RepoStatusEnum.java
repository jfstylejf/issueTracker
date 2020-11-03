package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepoStatusEnum {

    /**
     *
     */
    SCANNING ("scanning"),
    STOP("stop"),
    FAILED("failed"),
    COMPLETE("complete");

    private String type;
}
