package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
@AllArgsConstructor
public enum ScanStatusEnum {
    /**
     * issue scan status
     */
    DOING("doing"),
    COMPILE_FAILED("compile failed"),
    INVOKE_TOOL_FAILED("invoke tool failed"),
    ANALYZE_FAILED("analyze failed"),
    PERSIST_FAILED("persist failed"),
    MATCH_FAILED("match failed"),
    STATISTICAL_FAILED("statistical failed"),
    DONE("done");

    private final String type;
}
