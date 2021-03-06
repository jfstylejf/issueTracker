package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.bo.DeveloperLevel;
import cn.edu.fudan.measureservice.domain.bo.DeveloperRecentNews;
import cn.edu.fudan.measureservice.domain.dto.*;
import cn.edu.fudan.measureservice.domain.enums.DutyStatusEnum;
import cn.edu.fudan.measureservice.domain.enums.LanguageEnum;
import cn.edu.fudan.measureservice.domain.enums.ToolEnum;
import cn.edu.fudan.measureservice.mapper.AccountMapper;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    private AccountMapper accountMapper;

    private static final String split = ",";

    private static Map<String,RepoInfo> repoInfoMap = new HashMap<>(50);

    private static Map<String,List<RepoInfo>> projectInfo = new HashMap<>(20);

    private static Map<String, UserInfoDTO> userInfos = new ConcurrentHashMap<>(32);

    private static Map<String,Map<String,List<String>>> visibleProjectInfos = new ConcurrentHashMap<>(32);



    /**
     * 返回所参与repo下的所有开发者列表，若为 null,则删除
     *  @param query 查询条件
     * @return List<String> 返回开发者人员信息
     */
    public List<String> getDeveloperList(Query query) {
        List<String> list;
        List<String> developerGitNameList =  projectMapper.getCommitGitNameList(query.getRepoUuidList(),query.getSince(),query.getUntil());
        list = accountMapper.getAccountNameList(developerGitNameList);
        list.removeIf(Objects::isNull);
        return list;
    }

    /**
     * 获取查询库下的开发者列表
     * @param repoUuidList
     * @return
     */
    public Set<String> getDeveloperList(List<String> repoUuidList) {
        Set<String> developerList = new TreeSet<>(String::compareTo);
        for (String repoUuid : repoUuidList) {
            if (repoUuid == null) {
                continue;
            }
            List<String> temp = ((ProjectDao) AopContext.currentProxy()).getDeveloperList(repoUuid);
            developerList.addAll(temp);
        }
        return developerList;
    }

    @Cacheable(value = "repoDeveloper",key = "#repoUuid" ,condition = "#repoUuid != null")
    public List<String> getDeveloperList(String repoUuid) {
        Objects.requireNonNull(repoUuid);
        List<String> repoDeveloperGitNameList = projectMapper.getRepoCommitGitNameList(repoUuid);
        List<String> repoDeveloperList = accountMapper.getAccountNameList(repoDeveloperGitNameList);
        repoDeveloperList.removeIf(Objects::isNull);
        return repoDeveloperList;
    }



    /**
     * 获取开发者在职状态
     * @param developer 待验证开发者
     * @return 状态
     */
    @Cacheable(value = "developerDutyType",key = "#developer")
    public String getDeveloperDutyType(String developer) {
        String dutyType;
        String accountStatus = projectMapper.getDeveloperDutyType(developer);
        if("1".equals(accountStatus)) {
            dutyType = DutyStatusEnum.InPosition.getStatus();
        } else{
            dutyType = DutyStatusEnum.Resign.getStatus();
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
     * @return 项目参与库列表
     */
    @Cacheable(value = "projectRepo",key = "#projectName",condition = "#projectName != null ")
    public List<String> getProjectRepoList(String projectName) {
        try {
            List<String> projectRepoList =  projectMapper.getProjectRepoList(projectName);
            projectRepoList.removeIf(Objects::isNull);
            return projectRepoList;
        }catch (Exception e) {
            e.getMessage();
            return new ArrayList<>();
        }
    }

    /**
     * 获取项目的参与库列表
     * @param projectName 项目名
     * @return repoUuidList
     */
    @Deprecated
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
    public List<RepoInfo> getProjectInvolvedRepoInfo(String projectName,String token) {
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
     * fixme 改写存储逻辑，由 token 缓存项目
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
                if(repoUuid == null || repoInfoMap.containsKey(repoUuid)) {
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
     * 获取开发者的gitName列表
     * @param developer 开发者聚合后名
     * @return 包含 gitName 列表
     */
    @Cacheable(value = "developerGitNameList",key = "#developer")
    public List<String> getDeveloperGitNameList(String developer) {
        return accountMapper.getDeveloperAccountGitNameList(developer);
    }

    /**
     * 获取查询repoUuid的库名
     * @param repoUuid 查询库
     * @return  repoName
     */
    @Cacheable(value = "repoName",key = "#repoUuid")
    public String getRepoName(String repoUuid) {
        return projectMapper.getRepoName(repoUuid);
    }

    /**
     * 获取查询repoUuid的项目名
     * @param repoUuid 查询库
     * @return  projectName
     */
    @Cacheable(value = "projectName",key = "#repoUuid")
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
     * 获取用户权限可见的项目列表
     * @param token 查询token
     * @return projectList
     */
    @SuppressWarnings("unchecked")
    @Cacheable(value = "visibleProjectByToken",key = "'visibleProject_'+#token")
    public List<String> getVisibleProjectByToken(String token) {
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
    public List<String> mergeBetweenRepo(List<String> source,List<String> target) {
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
    /**
     * 去除source中不在target中的 project , 无则返回空列表
     * @param source 待去除项目
     * @param target 标志项目
     * @return List<String> source
     */
    public List<String> mergeBetweenProject(List<String> source, List<String> target) {
        Objects.requireNonNull(target,"the target list should not be null");
        if(target.size()==0) {
            if(source.size()!=0) {
                log.error("you dont have the authority to see the project : {}",source);
            }
            return new ArrayList<>();
        }
        for (int i = source.size()-1; i >= 0; i--) {
            if (!target.contains(source.get(i))) {
                log.error("you don't have the authority to see the project : {} !\n",source.remove(i));
            }
        }
        return source;
    }

    /**
     * 人员列表星级数据入库
     * @param developerLevelList
     * @return
     */
    public Boolean insertDeveloperLevel(List<DeveloperLevel> developerLevelList) {
        try {
            for (DeveloperLevel developerLevel : developerLevelList) {
                projectMapper.insertDeveloperLevel(developerLevel);
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<DeveloperLevel> getDeveloperLevelList(List<String> developerList) {
        try {
            return projectMapper.getDeveloperLevelList(developerList);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public Map<String,RepoInfo> getRepoInfoMap() {
        return repoInfoMap;
    }

    public Map<String,List<RepoInfo>> getProjectInfo() {
        return projectInfo;
    }

    /**
     * 根据 repoUuid 获取扫描工具名
     * @param repoUuid 查询库
     * @return String {@link ToolEnum}
     */
    public String getToolName(String repoUuid) {
        try {
            String language = projectMapper.getRepoLanguage(repoUuid);
            if (language.equals(LanguageEnum.Java.getType())) {
                return ToolEnum.JavaCodeAnalyzer.getType();
            }else if(language.equals(LanguageEnum.JAVASCRIPT.getType())) {
                return ToolEnum.JSCodeAnalyzer.getType();
            }else if (language.equals(LanguageEnum.CPP.getType())) {
                return ToolEnum.CppCodeAnalyzer.getType();
            }
            else {
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error("query baseDate failed!\n");
        }
        return null;
    }


    /**
     * 获取开发者在库下的最早提交时间
     * @param developer
     * @param repoUuid
     * @return
     */
    @Cacheable(value = "developerFirstCommitDate",key = "#developer+'_'+#repoUuid")
    public String getDeveloperFirstCommitDate(String developer, String repoUuid) {
        Objects.requireNonNull(developer);
        List<String> developerAccountGitNameList = ((ProjectDao) AopContext.currentProxy()).getDeveloperGitNameList(developer);
        return projectMapper.getDeveloperFirstCommitDate(repoUuid, developerAccountGitNameList);
    }

    /**
     * 获取开发者参与库的数量
     * @param developer
     * @return
     */
    @Cacheable(value = "developerInvolvedRepoNum",key = "#developer")
    public int getDeveloperInvolvedRepoNum(String developer) {
        List<String> gitNameList = ((ProjectDao) AopContext.currentProxy()).getDeveloperGitNameList(developer);
        return projectMapper.getDeveloperInvolvedRepoNum(gitNameList);
    }

    /**
     * 通过前端查询的项目列表和库列表，来获得可查询的库信息
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @param token 查询权限
     * @return 可查询库列表
     */
    @SneakyThrows
    public List<String> getVisibleRepoListByProjectNameAndRepo(String projectNameList, String repoUuidList, String token) {
        List<String> visibleProjectInvolvedRepoList;
        visibleProjectInvolvedRepoList = getVisibleRepoListByProjectName(projectNameList,token);
        List<String> visibleRepoList;
        // Case 1 : 若给定了查询库列表， 则根据查询库列表对 visibleProjectInvolvedRepoList 进行过滤，只取交集
        if (repoUuidList!=null && !"".equals(repoUuidList)) {
            List<String> queryRepoList = new ArrayList<>(Arrays.asList(repoUuidList.split(split)));
            queryRepoList.removeIf(o -> o == null || "".equals(o));
            visibleRepoList = mergeBetweenRepo(queryRepoList,visibleProjectInvolvedRepoList);
        }else {  // Case 2 : 若没有给定查询库列表， 则可查询库列表就为 visibleProjectInvolvedRepoList
            visibleRepoList = visibleProjectInvolvedRepoList;
        }
        return visibleRepoList;
    }

    /**
     * 通过项目列表查询可看库列表
     * @param projectNameList 项目列表
     * @param token 查询权限
     * @return 项目列表可查询库列表
     */
    public List<String> getVisibleRepoListByProjectName(String projectNameList, String token) {
        List<String> visibleProjectInvolvedRepoList;
        // Case 1 : 若查询项目列表不为空，则查询项目权限内可看库
        if(projectNameList!=null && !"".equals(projectNameList)) {
            visibleProjectInvolvedRepoList = new ArrayList<>();
            String[] projects = projectNameList.split(split);
            for (String projectName : projects) {
                visibleProjectInvolvedRepoList.addAll(getProjectRepoList(projectName,token));
            }
        }else {  // Case 2 : 若查询项目列表为空，则查询权限内可见所有项目的库列表
            visibleProjectInvolvedRepoList = involvedRepoProcess(null,token);
        }
        return visibleProjectInvolvedRepoList;
    }

    /**
     * 根据 projectIds 获取可查询的项目列表
     * @param projectIds 查询项目 Id
     * @param token 查询权限
     * @return {@link ProjectPair} checkedProjectPair
     */
    @SneakyThrows
    public List<ProjectPair> getVisibleProjectPairListByProjectIds(String projectIds, String token) {
        List<ProjectPair> projectPairList = new ArrayList<>();
        Map<String,Integer> queryProjectMap = getProjectNameListById(projectIds);
        // 内部调用走代理，否则缓存失效
        List<String> visibleProjectList = ((ProjectDao) AopContext.currentProxy()).getVisibleProjectByToken(token);
        List<String> checkedProjectList = mergeBetweenProject(new ArrayList<>(queryProjectMap.keySet()),visibleProjectList);
        for (String projectName : checkedProjectList) {
            projectPairList.add(new ProjectPair(projectName,queryProjectMap.get(projectName)));
        }
        return projectPairList;
    }


    /**
     * 根据查询项目Id, 获取 projectName 列表
     * @param projectIds 查询 projectId 列表
     * @return key : projectName, id
     */
    @SneakyThrows
    public Map<String,Integer> getProjectNameListById(String projectIds) {
        Map<String,Integer> map = new HashMap<>();
        List<String> projectIdList;
        if(projectIds!=null && !"".equals(projectIds)) {
            projectIdList = Arrays.asList(projectIds.split(split));
        }else {
            projectIdList = ((ProjectDao) AopContext.currentProxy()).getAllProjectId();
        }
        for (String projectId : projectIdList) {
            String projectName = ((ProjectDao) AopContext.currentProxy()).getProjectNameById(projectId);
            map.put(projectName,Integer.parseInt(projectId));
        }
        return map;
    }

    /**
     * 根据查询项目Id, 获取 projectName
     * @param projectId 查询 projectId
     * @return 所查项目名
     */
    @Cacheable(value = "projectNameById",key = "#projectId")
    public  String getProjectNameById(String projectId) {
        return projectMapper.getProjectNameById(projectId);
    }

    /**
     * 获取所有项目id
     * @return
     */
    @Cacheable(value = "allProject")
    public List<String> getAllProjectId() {
        return projectMapper.getAllProjectId();
    }

    /**
     * 根据 项目名 获取项目Id
     * @param projectName 查询项目名
     * @return int projectId
     */
    @Cacheable(value = "projectIdByName", key = "#projectName", condition = "#projectName != null ")
    public int getProjectIdByName(String projectName) {
        try {
            Integer id = projectMapper.getProjectIdByName(projectName);
            // 特判 projectName 是否在库种
            return id == null ? -1 : id;
        }catch (Exception e) {
            log.error("projectName wrong\n");
            return -1;
        }
    }

    /**
     * 获取开发者选定时间段内可见库列表
     * @param projectVisibleRepoList 项目可见库列表
     * @return
     */
    public List<String> getDeveloperVisibleRepo(List<String> projectVisibleRepoList,String developer, String since, String until) {
        List<String> developerGitNameList = ((ProjectDao) AopContext.currentProxy()).getDeveloperGitNameList(developer);
        List<String> developerRepoList = projectMapper.getDeveloperRepoList(developerGitNameList,since,until);
        return mergeBetweenRepo(developerRepoList,projectVisibleRepoList);
    }


    /**
     * 获取库列表所包含的提交次数
     * @param repoUuidList 查询库列表
     * @return
     */
    public int getRepoListMsgNum(List<String> repoUuidList,Boolean isValid) {
        int count = 0;
        for (String repoUuid : repoUuidList) {
            count += ((ProjectDao) AopContext.currentProxy()).getSingleRepoMsgNum(repoUuid,isValid);
        }
        return count;
    }

    /**
     * 获取一个库下的总提交次数
     * @param repoUuid 查询库 id
     * @return
     */
    public int getSingleRepoMsgNum(String repoUuid,Boolean isValid) {
        return projectMapper.getSingleProjectMsgNum(repoUuid,null,null,isValid);
    }


    /**
     * 获取开发者的最新提交
     * @param developer 查询开发者
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询截止时间
     * @return {@link DeveloperRecentNews} 开发者最新提交列表
     */
    public List<DeveloperRecentNews> getDeveloperRecentNews(String developer, List<String> repoUuidList, String since, String until) {
        List<String> developerGitNameList = ((ProjectDao) AopContext.currentProxy()).getDeveloperGitNameList(developer);
        return projectMapper.getDeveloperRecentNewsList(developerGitNameList,repoUuidList,since,until);
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
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}
