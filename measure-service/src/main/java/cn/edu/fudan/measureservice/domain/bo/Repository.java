package cn.edu.fudan.measureservice.domain.bo;



import cn.edu.fudan.measureservice.domain.d0.BaseData;
import cn.edu.fudan.measureservice.domain.d0.IssueInfo;
import cn.edu.fudan.measureservice.domain.d0.JiraInfo;
import cn.edu.fudan.measureservice.domain.d0.MeasureInfo;
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
        //CodeTracker codeTracker = new CodeTracker(query);
        //Clone clone = new Clone(query);
        JiraInfo jira = new JiraInfo(query);
        baseDataList.add(measure);
        init();
    }

    private void init(){
        for(BaseData m : baseDataList) {
            m.dataInjection();
        }
    }


}
