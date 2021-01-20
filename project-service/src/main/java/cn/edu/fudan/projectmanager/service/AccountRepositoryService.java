package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.domain.SubRepository;

import java.util.List;
import java.util.Map;

/**
 * description: 项目和库、负责人关系接口
 *
 * @author Richy
 * create: 2021-01-14 15:14
 **/
public interface AccountRepositoryService {

    /**
     * 获取库ID
     * @throws Exception e
     */
    String getRepoUuidByUuid(String projectUuid) throws Exception;

    /**
     * 获取库信息——通过人员ID
     * @throws Exception e
     */
    List<SubRepository> getRepoByAccountUuid(String accountUuid) throws Exception;

    /**
     * 获取库信息——通过库ID
     * @throws Exception e
     */
    SubRepository getRepoInfoByRepoId(String repoUuid) throws Exception;

    /**
     * 获取项目信息——通过人员名
     * @throws Exception e
     */
    List<Map<String, Object>> getProjectInfoByAccountName(String accountName) throws Exception;

    /**
     * 获取项目和库的对应关系
     * @throws Exception e
     */
    Map<String, List<Map<String, String>>> getProjectAndRepoRelation(int recycled) throws Exception;

    /**
     * 获取项目列表
     * @throws Exception e
     */
    List<Map<String, Object>> getProjectAll(String token) throws Exception;

    /**
     * 更新库所属项目
     * @param token 用户token
     * @param oldProjectName  旧名
     * @param newProjectName  新名
     * @throws Exception e
     */
    void updateRepoProject(String token, String oldProjectName, String newProjectName,String RepoUuid) throws Exception;

    /**
     * 新增项目负责人
     * @param token 用户token
     * @param newLeaderId  新负责人ID
     * @param projectId  项目ID
     * @throws Exception e
     */
    boolean addProjectLeader(String token, String newLeaderId, Integer projectId) throws Exception;

    /**
     * 删除项目负责人
     * @param token 用户token
     * @param LeaderId  负责人ID
     * @param projectId  项目ID
     * @throws Exception e
     */
    void deleteProjectLeader(String token, String LeaderId, Integer projectId) throws Exception;
}
