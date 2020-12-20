package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class ProjectDao {

    private RestInterfaceManager restInterface;

    private ProjectMapper projectMapper;

    private MeasureMapper measureMapper;

    /**
     * 返回所参与repo下的所有开发者列表
     *  @param query 查询条件
     * @return List<String> 返回开发者人员信息
     */
    public List<String> getDeveloperList(Query query) {
        return projectMapper.getDeveloperList(query.getRepoUuidList(),query.getSince(),query.getUntil());
    }

    /**
     * 根据开发者查询所参与的项目和库信息
     * @param query 查询条件
     * @return Map<String, List<String>>, key ：project_name, List<String>repoUuidList
     */
    public Map<String, List<String>> getProjectInfo(Query query) {
        Map<String, List<String>> result = new HashMap<>(0);
        List<Map<String, String>> maps = projectMapper.getProjectInfo(query.getDeveloper());
        for (Map<String, String> map : maps) {
            String projectName = map.get("project_name");
            String repoUuid = map.get("repo_uuid");
            if (! result.containsKey(projectName)) {
                result.put(projectName, new ArrayList<>(8));
            }
            List<String> repoUuidList = result.get(projectName);
            repoUuidList.add(repoUuid);
        }
        return result;
    }

    /**
     * 根据参与项目名，获取开发者
     * @param projectName 项目名
     * @param token 查询密钥
     * @return List<String> developerInvolvedRepoList
     */
    private List<String> getDeveloperProjectInvolvedRepoList(String projectName,String token) {
        Objects.requireNonNull(projectName,"projectName should not be null when get developerInvolvedRepoList by projectName");
        List<String> developerInvolvedRepoList = new ArrayList<>();
        List<Map<String,String>> response = restInterface.getProjectInfo(projectName,token);
        if(response == null) {
            log.error("cannot get projectInfo");
            return null;
        }
        for(int i=0 ;i<response.size();i++){
            Map<String,String> repo = response.get(i);
            String repoUuid = repo.get("repo_id");
            if(repoUuid!=null && !"".equals(repoUuid)){
                developerInvolvedRepoList.add(repoUuid);
            }
        }
        return developerInvolvedRepoList;
    }

    /**
     * 获取开发者参与库列表（且在sub_repository下）
     * @param query 查询条件
     * @return List<String> developerRepoList
     */
    private List<String> getDeveloperRepoList(Query query) {
        return projectMapper.getDeveloperRepoList(query.getDeveloper(),query.getSince(),query.getUntil());
    }

    /**
     * 获取开发者参与库的合法提交信息（去除Merge）
     * @param query 查询条件
     * @return List<Map<String,Object>> key : developer_unique_name , commit_time , commit_id , message
     */
    public List<Map<String,Object>> getValidCommitMsg(Query query) {
        return projectMapper.getValidCommitMsg(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
    }


    /**
     * 获取查询repoUuid的库名
     * @param repoUuid 查询库
     * @return String repoName
     */
    public String getRepoName(String repoUuid) {
        return projectMapper.getRepoName(repoUuid);
    }

    /**
     * 获取查询repoUuid的项目名
     * @param repoUuid 查询库
     * @return String projectName
     */
    public String getProjectName(String repoUuid) {
        return projectMapper.getProjectName(repoUuid);
    }

    /**
     * 获取查询条件下提交次数前3名的开发者
     * @param query 查询条件
     * @return key : developerName , countNum
     */
    public List<Map<String, Object>> getDeveloperRankByCommitCount(Query query) {
        List<Map<String,Object>> list = new ArrayList<>();
        List<Map<String,Object>> temp = projectMapper.getDeveloperRankByCommitCount(query.getRepoUuidList(),query.getSince(),query.getUntil());
        for(Map<String,Object> m : temp) {
            if(m.size()<2) {
                m.put("developer_name","");
            }
            list.add(m);
        }
        return list;
    }

    /**
     * 获取开发者该库下的总提交次数
     * @param query 查询条件
     * @return int developerCommitCount
     */
    public int getDeveloperCommitCountsByDuration(Query query) {
        return projectMapper.getDeveloperCommitCountsByDuration(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
    }


    /**
     * 获取项目的参与库列表
     * @param query 查询条件
     * @param projectName 项目名
     * @return repoUuidList
     */
    public List<String> getProjectIntegratedRepoList(Query query,String projectName) {
        List<String> repoUuidList ;
        if(query.getRepoUuidList()!=null && query.getRepoUuidList().size()>0) {
            repoUuidList = query.getRepoUuidList();
        }else if(projectName!=null && !"".equals(projectName)) {
            repoUuidList = getDeveloperProjectInvolvedRepoList(projectName,query.getToken());
            if(repoUuidList==null || repoUuidList.size()==0) {
                log.warn("is this project have no repo ?");
                return null;
            }
        }else {
            repoUuidList = getDeveloperRepoList(query);
        }
        return repoUuidList;
    }

    /**
     * 删除所属repo下repo_measure表数据
     * @param query 查询条件
     */
    public void deleteRepoMsg(Query query) {
        int countNum = measureMapper.getMsgNumByRepo(query.getRepoUuidList());
        try {
            while (countNum > 0) {
                countNum -= 5000;
                projectMapper.deleteRepoMsg(query.getRepoUuidList());
            }
            log.info("delete repoMsg from repo_measure Success!");
        }catch (Exception e) {
            e.getMessage();
            log.error("delete repoMsg from repo_measure Failed");
        }
    }

    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {
        this.restInterface = restInterface;
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Autowired
    public void setMeasureMapper(MeasureMapper measureMapper) {
        this.measureMapper = measureMapper;
    }
}
