package cn.edu.fudan.issueservice.domain.enums;

/**
 * @author Beethoven
 */
public enum LanguageEnum {
    /**
     * 扫描支持的语言
     */
    Java("java"),
    JavaScript("javascript");

    private final String name;

    LanguageEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
