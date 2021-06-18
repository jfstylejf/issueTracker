package cn.edu.fudan.measureservice.domain.enums;

/**
 * @author wjzho
 */

public enum ScanStatusEnum {
    /**
     * 扫描中
     */
    SCANNING("scanning"),
    /**
     * 调用扫描工具失败
     */
    INVOKE_TOOL_FAILED("invoke tool failed"),
    /**
     * 扫描数据解析中
     */
    ANALYZING("analyzing"),
    /**
     * 解析过程失败
     */
    ANALYZE_FAILED("analyze failed"),
    /**
     * 扫描完成
     */
    DONE("complete");

    private final String type;

    ScanStatusEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


}
