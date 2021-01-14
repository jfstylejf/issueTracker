package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.RepositoryDTO;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * description: 项目下载 更新等接口
 *
 * @author fancying
 * create: 2020-09-27 15:38
 **/
public interface ProjectControlService {


    /**
     * 添加一个仓库
     * @param token 用户token
     * @param repositoryDTO repo信息
     * @throws Exception e
     */
    void addOneRepo(String token, RepositoryDTO repositoryDTO) throws Exception;

    /**
     * 本地添加一个仓库
     * @param token 用户token
     * @param repositoryDTO repo信息
     * @throws Exception e
     */
    void addOneRepoByLocal(String token, RepositoryDTO repositoryDTO) throws Exception;


    /**
     * 添加多个仓库
     * @param token 用户token
     * @param  repositories repo信息
     * @return Map k v
     * @throws Exception e
     */
    Map<String, Boolean> addRepos(String token, List<RepositoryDTO> repositories);


    /**
     * 更新项目信息
     * @param token 用户token
     * @param oldProjectName 旧名
     * @param newProjectName  新名
     * @throws Exception e
     */
    void update(String token, String oldProjectName, String newProjectName) throws Exception;

    /**
     * 删除库
     * @param token 用户token
     * @param subRepoId repo信息
     * @param empty 是否需要清空回收站
     * @throws Exception e
     */
    void delete(@NotNull String token, String subRepoId, Boolean empty) throws Exception;

    /**
     * 查询项目
     * @param token 用户token
     * @throws Exception e
     */
    List<SubRepository> query(String token);

    /**
     * 添加一个项目
     * @param token 用户token
     * @param projectName 项目名
     * @throws Exception e
     */
    void addOneProject(String token, String projectName) throws Exception;

    /**
     * 获取全部项目
     * @throws Exception e
     */
    List<Map<String, Object>> getProjectAll(String token);

    /**
     * 删除项目
     * @param token 用户token
     * @param projectName projectName
     * @throws Exception e
     */
    void deleteProject(String token, String projectName) throws Exception;

    /**
     * 更新库信息
     * @param token 用户token
     * @param oldRepoName 旧名
     * @param newRepoName  新名
     * @throws Exception e
     */
    void updateRepo(String token, String oldRepoName, String newRepoName) throws Exception;

    /**
     * 更新库所属项目
     * @param token 用户token
     * @param oldProjectName  旧名
     * @param newProjectName  新名
     * @throws Exception e
     */
    void updateRepoProject(String token, String oldProjectName, String newProjectName,String RepoUuid) throws Exception;

    /**
     * 删除库
     * @param token 用户token
     * @param repoUuid repo uuid
     * @throws Exception e
     */
    void deleteRepo(@NotNull String token, String repoUuid) throws Exception;

    /**
     * 修改项目负责人
     * @param token 用户token
     * @param newLeaderId  新负责人ID
     * @param projectId  项目ID
     * @throws Exception e
     */
    void addProjectLeader(String token, String newLeaderId, String projectId) throws Exception;
}
