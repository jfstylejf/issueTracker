package cn.edu.fudan.measureservice.domain.enums;

/**
 * @author wjzho
 */
public enum ToolEnum {
    // js代码解析
    JSCodeAnalyzer("eslint"),
    // java代码解析
    JavaCodeAnalyzer("javancss");

    private final String type;

    ToolEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
