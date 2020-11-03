package cn.edu.fudan.issueservice.domain.enums;

/**
 * @author fancying
 */
public enum RestServiceEnum {
    ACCOUNT_SERVICE("account"),
    PROJECT_SERVICE("project");

    private String name;
    RestServiceEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
