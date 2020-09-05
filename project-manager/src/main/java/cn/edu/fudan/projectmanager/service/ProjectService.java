package cn.edu.fudan.projectmanager.service;


import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.domain.RepoBasicInfo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProjectService {

    void addOneProject(String userToken, JSONObject projectInfo);

    JSONObject addProjectList(String userToken, List<JSONObject> projectInfo);

    Object getProjectList(String userToken,String type, int isRecycled);

    List<JSONObject> getProjectListInfoFromExcelFile(MultipartFile file) throws IOException;

    //jeff
    Object getProjectListByModule(String userToken, String type, String module);

    Object getProjectByRepoId(String repo_id);

    Object getProjectByAccountId(String account_id, int isRecycled);

    Object getProjectListByKeyWord(String userToken, String module,String keyWord,String type,int isRecycled);

    List<String> getRepoIdsByAccountIdAndType(String account_id,String type, int isRecycled);

    Project getProjectByID(String projectId);

    void updateProjectStatus(Project project);

    void remove(String projectId,String type, String userToken);

    String getRepoId(String projectId);

    Object existProjectWithThisRepoIdAndType(String repoId,String type,boolean isFirst);

    void updateProjectFirstAutoScan(String repoId,String type);

    Project getProjectByRepoIdAndCategory(String userToken,String repoId,String category);

    /**
     * 根据条件筛选出相应的project列表
     * @param userToken
     * @param category
     * @param name
     * @param module
     * @return 返回符合条件的project列表
     */
    List<Project> getProjectsByCondition(String userToken,String category,String name,String module);

    /**
     * 每当用户添加项目时，若管理员没有此项目，也为管理员添加一次
     * @param projectId
     */
    void addRootProject(String projectId);

    void removeNonAdminProject(String projectId,String type, String userToken);

    /**
     *
     * @return
     */
    List<Project> getAllProject(int isRecycled);

    /**
     * 管理员：项目回收功能，即删除项目进入回收站，将项目repo相同的项目全部进入回收站；
     * 由项目表中recycled字段表示，1已回收状态，0表示没有被回收
     * @param projectId
     * @param userToken
     */
    void recycle(String projectId, String userToken, int isRecycled);

    void updateProject(JSONObject project);

    public List<Map<String, Object>> getProjectInfoByAccountName(String accountName);
}
