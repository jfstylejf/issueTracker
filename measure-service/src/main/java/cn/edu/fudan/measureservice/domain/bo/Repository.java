package cn.edu.fudan.measureservice.domain.bo;



import cn.edu.fudan.measureservice.domain.d0.*;
import cn.edu.fudan.measureservice.domain.dto.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjzho
 */

public class Repository {

    private Query query;
    private String firstCommitDate;
    private String projectName;
    private String repoName;
    private List<BaseData> baseDataList;

    public Repository(Query query, String repoName, String projectName) {
        this.query = query;
        this.projectName = projectName;
        this.repoName = repoName;
        baseDataList = new ArrayList<>();
        MeasureInfo measure = new MeasureInfo(query);
        IssueInfo issue = new IssueInfo(query);
        CodeTracker codeTracker = new CodeTracker(query);
        CloneInfo clone = new CloneInfo(query);
        JiraInfo jira = new JiraInfo(query);
        baseDataList.add(measure);
        baseDataList.add(issue);
        baseDataList.add(codeTracker);
        baseDataList.add(clone);
       // baseDataList.add(jira);
        init();
    }

    private void init(){
        for(BaseData m : baseDataList) {
            m.dataInjection();
        }
    }


}
