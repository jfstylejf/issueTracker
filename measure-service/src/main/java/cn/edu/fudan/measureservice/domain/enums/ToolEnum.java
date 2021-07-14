package cn.edu.fudan.measureservice.domain.enums;

import cn.edu.fudan.measureservice.core.process.CppCodeAnalyzer;

/**
 * @author wjzho
 */
public enum ToolEnum {
    // js代码解析
    JSCodeAnalyzer("eslint"),
    // java代码解析
    JavaCodeAnalyzer("javancss"),
    // c++ 代码解析
    CppCodeAnalyzer("cpp14Parser")
    ;

    private final String type;

    ToolEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
