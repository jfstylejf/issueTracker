package cn.edu.fudan.issueservice.domain.enums;


import lombok.Getter;

@Getter
public enum IssueTypeEnum {

    /**
     * BUG,CODE_SMELL,SECURITY_HOTSPOT,VULNERABILITY  属于sonar issue type
     *
     * MALICIOUS_CODE_VULNERABILITY,DODGY_CODE,SECURITY,INTERNATIONALIZATION,CORRECTNESS,BAD_PRACTICE,PERFORMANCE,
     * MULTITHREADED_CORRECTNESS,EXPERIMENTAL   属于 findbugs issue type
     */

    BUG("Bug","sonarqube","security"),
    CODE_SMELL("Code smell","sonarqube","standard"),
    SECURITY_HOTSPOT("Security hotspot","sonarqube","security"),
    VULNERABILITY("Vulnerability","sonarqube","security"),

    MALICIOUS_CODE_VULNERABILITY("Malicious code vulnerability","findbugs","security"),
    DODGY_CODE("Dodgy code","findbugs","standard"),
    SECURITY("Security","findbugs","security"),
    INTERNATIONALIZATION("Internationalization","findbugs","standard"),
    CORRECTNESS("Correctness","findbugs","security"),
    BAD_PRACTICE("Bad practice","findbugs","standard"),
    PERFORMANCE("Performance","findbugs","standard"),
    MULTITHREADED_CORRECTNESS("Multithreaded correctness","findbugs","security"),
    EXPERIMENTAL("Experimental","findbugs","standard");




    private String name;
    private String category;
    private String tool;
    IssueTypeEnum(String name,String tool,String category) {
        this.name = name;
        this.category = category;
        this.tool = tool;
    }


    public static IssueTypeEnum getIssueTypeEnum(String name){
        for(IssueTypeEnum issueTypeEnum : IssueTypeEnum.values()){
            if(issueTypeEnum.getName().equals(name)){
                return issueTypeEnum;
            }
        }
        return null;
    }
}
