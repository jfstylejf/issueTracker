package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.DeveloperRepoInfo;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.dto.RepoInfo;
import cn.edu.fudan.measureservice.domain.dto.UserInfoDTO;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wjzho
 */
@Slf4j
@Getter
@Repository
public class ProjectDao {

    private RestInterfaceManager restInterface;

    private ProjectMapper projectMapper;

    private MeasureMapper measureMapper;

    private static final String split = ",";

    private static Map<String,RepoInfo> repoInfoMap = new HashMap<>(50);

    private static Map<String,List<RepoInfo>> projectInfo = new HashMap<>(20);

    private static Map<String, UserInfoDTO> userInfos = new ConcurrentHashMap<>(32);

    private static Map<String,Map<String,List<String>>> visibleProjectInfos = new ConcurrentHashMap<>(32);


    /**
     * 返回所参与repo下的所有开发者列表
     *  @param query 查询条件
     * @return List<String> 返回开发者人员信息
     */
    public List<String> getDeveloperList(Query query) {
        return projectMapper.getDeveloperList(query.getRepoUuidList(),query.getSince(),query.getUntil());
    }

    /**
     * fixme
     * 获取开发者在参与库中的信息
     * @param query 查询条件
     * @return  Map<String,List<DeveloperRepoInfo>> developerRepoInfos
     */
    public Map<String,List<DeveloperRepoInfo>> getDeveloperRepoInfoList(Query query) {
        Map<String,List<DeveloperRepoInfo>> developerRepoInfos = new HashMap<>(50);
        List<String> repoUuidList = query.getRepoUuidList();
        if(repoUuidList.size()==0) {
            log.warn("do not have repoInfo !\n");
            return null;
        }
        for(String repoUuid : repoUuidList) {
            if(!repoInfoMap.containsKey(repoUuid)) {
                insertProjectInfo(query.getToken());
            }
            List<Map<String,String>> developerRepoInfoList = projectMapper.getDeveloperRepoInfoList(repoUuid,query.getSince(),query.getUntil());
            repoInfoMap.get(repoUuid).setInvolvedDeveloperNumber(developerRepoInfoList.size());
            for(Map<String,String> map : developerRepoInfoList) {
                String developerName = map.get("developer_unique_name");
                if(developerName==null || "".equals(developerName)) {
                    continue;
                }
                if(!developerRepoInfos.containsKey(developerName)) {
                    developerRepoInfos.put(developerName,new ArrayList<>());
                }
                developerRepoInfos.get(developerName).add(new DeveloperRepoInfo(developerName,repoInfoMap.get(repoUuid),map.get("firstCommitDate")));
            }
        }
        return developerRepoInfos;
    }

    /**
     * 获取开发者在职状态
     * @param developerList 待验证开发者列表
     * @return Map<String, String> key : 开发者名，状态
     */
    public Map<String, String> getDeveloperDutyType(Set<String> developerList) {
        Map<String,String> dutyType = new HashMap<>(50);
        List<Map<String,String>> developerDutyList = projectMapper.getDeveloperDutyTypeList();
        for(Map<String,String> map : developerDutyList) {
            dutyType.put(map.get("account_name"),map.get("account_status"));
        }
        return dutyType;
    }

    /**
     * fixme 处理空列表的情况
     * 获得查询条件下所参与列表（ <= leader所管理库的数量 ）
     * @param repoUuidList 所输入repo列表
     * @param token 身份鉴定
     * @return List<String> repoUuidList
     */
    public List<String> involvedRepoProcess(String repoUuidList,String token) {
        List<String> repoList;
        try {
            List<String> leaderIntegratedRepoList = getVisibleRepoInfoByToken(token);
            if(repoUuidList!=null && !"".equals(repoUuidList)) {
                repoList = Arrays.asList(repoUuidList.split(split));
            }else {
                repoList = leaderIntegratedRepoList;
            }
            return mergeBetweenRepo(repoList,leaderIntegratedRepoList);
        }catch (Exception e) {
            e.getMessage();
        }
        return new ArrayList<>();
    }

    /**
     * 转换为查询库列表
     * @param repoInfos 查询库信息
     * @return repoUuidList
     */
    private List<String> transferRepoInfoToRepoList(List<RepoInfo> repoInfos) {
        List<String> repoUuidList = new ArrayList<>();
        for(RepoInfo repoInfo : repoInfos) {
            repoUuidList.add(repoInfo.getRepoUuid());
        }
        return repoUuidList;
    }

    /**
     * 返回用户鉴权后的库信息
     * @param token 查询token
     * @return repoUuidList
     */
    public List<String> getVisibleRepoInfoByToken(String token) {
        List<String> repoUuidList = new ArrayList<>();
        if(visibleProjectInfos.containsKey(token)) {
            Map<String,List<String>> visibleProjectInfo = visibleProjectInfos.get(token);
            for(String projectName : visibleProjectInfo.keySet()) {
                repoUuidList.addAll(visibleProjectInfo.get(projectName));
            }
            return repoUuidList;
        }
        visibleProjectInfos.put(token,new HashMap<>(20));
        for(String projectName : getVisibleProjectByToken(token)) {
            if(!projectInfo.containsKey(projectName)) {
                insertProjectInfo(token);
            }
            List<String> temp = transferRepoInfoToRepoList(projectInfo.get(projectName));
            visibleProjectInfos.get(token).put(projectName,temp);
            repoUuidList.addAll(temp);
        }
        return repoUuidList;
    }

    /**
     * 获取项目的参与库列表
     * @param projectName 项目名
     * @return repoUuidList
     */
    public List<String> getProjectRepoList(String projectName,String token) {
        List<String> repoUuidList = new ArrayList<>();
        List<RepoInfo> repoInfos = getProjectInvolvedRepoInfo(projectName,token);
        if(repoInfos.size()==0) {
            log.warn("is this project : {} has no repo ?",projectName);
            return new ArrayList<>();
        }
        for(RepoInfo repoInfo : repoInfos) {
            repoUuidList.add(repoInfo.getRepoUuid());
        }
        return repoUuidList;
    }


    /**
     * 根据参与项目名，获得对应的库信息
     * @param projectName 项目名
     * @param token 查询密钥
     * @return List<RepoInfo>
     */
    private List<RepoInfo> getProjectInvolvedRepoInfo(String projectName,String token) {
        Objects.requireNonNull(projectName,"projectName should not be null when get developerInvolvedRepoList by projectName");
        if(!projectInfo.containsKey(projectName)) {
            if(!insertProjectInfo(token)){
                log.error("CANNOT INSERT projectInfo, check again !\n");
                return new ArrayList<>();
            }
        }
        return projectInfo.get(projectName);
    }

    /**
     * fixme 方法加个init方法,可以重置项目信息
     * 初始化项目信息
     * @param token 查询token
     * @return Boolean 添加状态
     */
    public Boolean insertProjectInfo(String token) {
        Map<String,List<Map<String,String>>> response = restInterface.getProjectInfo(token);
        if(response==null) {
            log.error("REST REQUEST failed to getProjectInfo\n");
            return false;
        }
        for(Map.Entry<String,List<Map<String,String>>> entry : response.entrySet()) {
            if(!projectInfo.containsKey(entry.getKey())) {
                projectInfo.put(entry.getKey(),new ArrayList<>());
            }
            for(Map<String,String> repo : entry.getValue()) {
                String repoUuid = repo.get("repo_id");
                if(repoInfoMap.containsKey(repoUuid)) {
                    //已经更新，不存储该库数据
                    continue;
                }
                RepoInfo repoInfo = new RepoInfo(entry.getKey(),repo.get("name"),repo.get("repo_id"),0);
                repoInfoMap.put(repoUuid,repoInfo);
                projectInfo.get(entry.getKey()).add(repoInfo);
            }
        }
        return true;
    }

    /**
     * 获取开发者参与库的合法提交信息（去除Merge）
     * @param query 查询条件
     * @return List<Map<String,Object>> key : developer_unique_name , commit_time , commit_id , message
     */
    public List<Map<String,String>> getValidCommitMsg(Query query) {
        return projectMapper.getValidCommitMsg(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
    }


    /**
     * 获取查询repoUuid的库名
     * @param repoUuid 查询库
     * @return String repoName
     */
    @Deprecated
    public String getRepoName(String repoUuid) {
        return projectMapper.getRepoName(repoUuid);
    }

    /**
     * 获取查询repoUuid的项目名
     * @param repoUuid 查询库
     * @return String projectName
     */
    @Deprecated
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

    /**
     * 获取用户权限可见的项目列表
     * @param token 查询token
     * @return projectList
     */
    @SuppressWarnings("unchecked")
    private List<String> getVisibleProjectByToken(String token) {
        UserInfoDTO userInfoDTO = null;
        try {
            userInfoDTO = getUserInfoByToken(token);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        if(userInfoDTO == null) {
            log.warn("no userInfo, token : {}",token);
            return new ArrayList<>();
        }
        //用户权限为admin时 查询所有的repo
        if (userInfoDTO.getRight().equals(0)) {
            insertProjectInfo(token);
            List<String> list = new ArrayList<>();
            list.addAll(projectInfo.keySet());
            return list;
        }else {
            String userUuid = userInfoDTO.getUuid();
            return projectMapper.getProjectByAccountId(userUuid);
        }
    }

    /**
     * 查看访问用户的权限
     * @param token 查询token
     * @return userInfoDTO
     * @throws Exception
     */
    private synchronized UserInfoDTO getUserInfoByToken(String token) throws Exception{
        if (org.springframework.util.StringUtils.isEmpty(token)) {
            throw new RuntimeException("need user token");
        }
        if (userInfos.containsKey(token)) {
            return userInfos.get(token);
        }
        UserInfoDTO userInfoDTO = restInterface.getUserInfoByToken(token);
        if (userInfoDTO == null) {
            throw new RuntimeException("get user info failed");
        }
        userInfos.put(token, userInfoDTO);
        return userInfoDTO;
    }

    /**
     * 去除source中不在target中的repo, 无则返回空列表
     * @param source 待去除库
     * @param target 标志库
     * @return List<String> source
     */
    private List<String> mergeBetweenRepo(List<String> source,List<String> target) {
        Objects.requireNonNull(target,"the target list should not be null");
        if(target.size()==0) {
            if(source.size()!=0) {
                log.error("you dont have the authority to see the repo : {}",source);
            }
            return new ArrayList<>();
        }
        source.removeIf(o -> !target.contains(o));
        return source;
    }


    public Map<String,RepoInfo> getRepoInfoMap() {
        return repoInfoMap;
    }

    public Map<String,List<RepoInfo>> getProjectInfo() {
        return projectInfo;
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