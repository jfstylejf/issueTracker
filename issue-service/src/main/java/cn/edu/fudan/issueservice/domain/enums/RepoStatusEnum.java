package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
@AllArgsConstructor
public enum RepoStatusEnum {

    /**
     * repo cn.edu.fudan.common.scan status
     */
    SCANNING ("scanning"),
    STOP("stop"),
    FAILED("failed"),
    COMPLETE("complete");

    private final String type;
}
