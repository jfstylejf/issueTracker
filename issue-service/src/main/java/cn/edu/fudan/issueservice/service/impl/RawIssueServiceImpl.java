package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.annotation.FreeResource;
import cn.edu.fudan.issueservice.annotation.GetResource;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.domain.enums.RepoNatureEnum;
import cn.edu.fudan.issueservice.service.RawIssueService;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private Logger logger = LoggerFactory.getLogger(RawIssueServiceImpl.class);

    private IssueRepoDao issueRepoD;
    private RestInterfaceManager restInterfaceManager;
    private RawIssueDao rawIssueDao;
    private LocationDao locationDao;


    /**
     * 删除rawIssue以及对应的locations
     *
     * @param repoId 项目的id
     * @author WZY
     */
    @Transactional
    @Override
    public void deleteRawIssueByRepoIdAndTool(String repoId,String tool) {
        locationDao.deleteLocationByRepoIdAndCategory(repoId,tool);
        rawIssueDao.deleteRawIssueByRepoIdAndTool(repoId,tool);
    }


    @Override
    public Map<String, Object> getCode(String project_id, String commit_id, String file_path) {
//        file_path=file_path.substring(file_path.indexOf("/")+1);
        Map<String, Object> result = new HashMap<>();
        String repo_id =restInterfaceManager.getRepoIdOfProject(project_id);
        String repoHome=null;
        try{
            JSONObject response = restInterfaceManager.getRepoPath(repo_id,commit_id).getJSONObject("data");
            logger.info(response.toJSONString());
            if (response != null && response.getString("status").equals("Successful")) {
                repoHome=response.getString("content");
                logger.info("repoHome -> {}" ,repoHome);
                result.put("code", getFileContent(repoHome+"/"+file_path));
            } else {
                result.put("code", "");
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }finally {
            if(repoHome!=null){
                JSONObject response =restInterfaceManager.freeRepoPath(repo_id,repoHome);
                if (response != null && response.getJSONObject("data").getString("status").equals("Successful")) {
                    logger.info("{} free success",repoHome);
                } else {
                    logger.info("{} free failed",repoHome);
                }
            }

        }
        return result;
    }


    @Override
    public Object getRawIssueList(String issue_id,Integer page,Integer size,String status){
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
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
        param.put("issue_id",issue_id);
        param.put("page",page);
        param.put("size",size);
        /*获取总页数*/
        int count = rawIssueDao.getNumberOfRawIssuesByIssueIdAndStatus(issue_id,statusList);
        param.put("start", (page - 1) * size);
        param.put("statusList",statusList);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        List<RawIssue> rawIssues = rawIssueDao.getRawIssueListByIssueId(param);
        if(rawIssues.size() != 0){
            JSONObject repoPathJson = null;
            String repoPath = null;
            JGitHelper jGitHelper = null;
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

    @Deprecated
    @Override
    public List<RawIssue> getRawIssueByIssueId(String issueId) {
        return rawIssueDao.getRawIssueByIssueId(issueId);
    }

    private String getFileContent(String filePath){
        StringBuilder code = new StringBuilder();
        String s = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while ((s = bufferedReader.readLine()) != null) {
                code.append(s);
                code.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code.toString();
    }

    private void filterRawIssueFileName(List<RawIssue> rawIssues){
        if(rawIssues != null && !rawIssues.isEmpty ()){
            for(RawIssue rawIssue : rawIssues){
                String fileName = rawIssue.getFile_name ();
                String[] filePath = fileName.split ("/");
                fileName = filePath[filePath.length-1];
                rawIssue.setFile_name (fileName);
            }
        }

    }


    @Autowired
    public void setIssueRepoD(IssueRepoDao issueRepoD) {
        this.issueRepoD = issueRepoD;
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
