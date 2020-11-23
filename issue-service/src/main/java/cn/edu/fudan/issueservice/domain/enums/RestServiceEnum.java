package cn.edu.fudan.issueservice.domain.enums;

/**
 * @author fancying
 */
public enum RestServiceEnum {
    /**
     * rest service
     */
    ACCOUNT_SERVICE("account"),
    PROJECT_SERVICE("project");

    private final String name;
    RestServiceEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
