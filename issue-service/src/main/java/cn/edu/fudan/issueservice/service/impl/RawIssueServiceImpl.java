package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.service.RawIssueService;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class RawIssueServiceImpl implements RawIssueService {

    private RestInterfaceManager restInterfaceManager;

    private RawIssueDao rawIssueDao;

    private LocationDao locationDao;

    @Override
    public Object getRawIssueList(String issueId, Integer page, Integer size, String status){
        Map<String, Object> result = new HashMap<>(8);
        Map<String, Object> param = new HashMap<>(12);
        List<String> statusList = new ArrayList<>();
        if(status != null){
            String[] statusArray =  status.split(",");
            for(String statusJudge : statusArray){
                RawIssueStatus rawIssueStatus = RawIssueStatus.getStatusByName(statusJudge);
                if(rawIssueStatus == null ){
                    return statusJudge + " is wrong, please input add ,changed or solved.";
                }
                statusList.add(statusJudge);
            }
            param.put("list",statusList);
        }
        param.put("issue_id",issueId);
        param.put("page",page);
        param.put("size",size);
        /*获取总页数*/
        int count = rawIssueDao.getNumberOfRawIssuesByIssueIdAndStatus(issueId,statusList);
        param.put("start", (page - 1) * size);
        param.put("statusList",statusList);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        List<RawIssue> rawIssues = rawIssueDao.getRawIssueListByIssueId(param);
        if(rawIssues.size() != 0){
            JSONObject repoPathJson;
            String repoPath = null;
            JGitHelper jGitHelper;
            String repoId = rawIssues.get(0).getRepo_id();
            String commitId = rawIssues.get(rawIssues.size()-1).getCommit_id();
            try{
                repoPathJson = restInterfaceManager.getRepoPath(repoId,commitId);
                if(repoPathJson == null){
                    throw new RuntimeException("can not get repo path");
                }
                repoPath = repoPathJson.getJSONObject("data").getString("content");
                if(repoPath == null){
                    throw new RuntimeException("can not get repo path");
                }
                jGitHelper = new JGitHelper(repoPath);
                jGitHelper.checkout(rawIssues.get(0).getCommit_id());

                for(RawIssue rawIssue : rawIssues){
                    rawIssue.setDeveloperName(jGitHelper.getAuthorName(rawIssue.getCommit_id()));
                }
            }finally{
                if(repoPath!= null){
                    restInterfaceManager.freeRepoPath(repoId,repoPath);
                }
            }
        }

        result.put("rawIssueList",rawIssues);

        return result;
    }

    @Override
    public List<Map<String, Object>> getRawIssueByIssueId(String issueUuid) {

        List<Map<String, Object>> rawIssues = rawIssueDao.getRawIssueByIssueId(issueUuid);

        rawIssues.forEach(rawIssue -> rawIssue.put("location", locationDao.getLocationsByRawIssueUuid((String) rawIssue.get("uuid"))));

        return rawIssues;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}
