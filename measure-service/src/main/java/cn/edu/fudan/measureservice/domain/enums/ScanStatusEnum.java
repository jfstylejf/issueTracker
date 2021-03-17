package cn.edu.fudan.measureservice.domain.enums;

/**
 * @author wjzho
 */

public enum ScanStatusEnum {

    SCANNING("scanning"),
    INVOKE_TOOL_FAILED("invoke tool failed"),
    ANALYZING("analyzing"),
    ANALYZE_FAILED("analyze failed"),
    DONE("complete");

    private final String type;

    ScanStatusEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


}
