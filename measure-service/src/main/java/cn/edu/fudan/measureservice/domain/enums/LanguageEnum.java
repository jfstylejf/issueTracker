package cn.edu.fudan.measureservice.domain.enums;

/**
 * @ClassName: LanguageEnum
 * @Description: 扫描支持语言
 * @Author wjzho
 * @Date 2021/7/8
 */

public enum LanguageEnum {

    // Java 语言
    Java("Java"),
    // Js 语言
    JAVASCRIPT("JavaScript"),
    // c++ 语言
    CPP("C++")
    ;

    private String type;

    LanguageEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
