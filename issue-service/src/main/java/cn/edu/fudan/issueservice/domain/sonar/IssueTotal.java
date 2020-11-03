package cn.edu.fudan.issueservice.domain.sonar;

/**
 * description: 记录访问/api/issues/search的结果 {@link http://10.141.221.85:9000/web_api/api/issues/search}
 *
 * @author fancying
 * create: 2020-07-14 09:48
 **/
public class IssueTotal {
    int total;
    int pageIndex;
    /**
     * Maximum value 500 Default value 100
     */
    int pageSize;

    int effortTotal;
    int debtTotal;

//    List<SonarIssue> sonarIssueList;
//    List<SonarComponent> componentList;

    Object facets;
}