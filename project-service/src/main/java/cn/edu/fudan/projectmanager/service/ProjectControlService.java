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
     * 添加多个仓库
     * @param token 用户token
     * @param  repositories repo信息
     * @return Map k v
     * @throws Exception e
     */
    Map<String, Boolean> addRepos(String token, List<RepositoryDTO> repositories);


    /**
     * 更新仓库信息
     * @param token 用户token
     * @param oldProjectName 旧名
     * @param newProjectName  新名
     * @throws Exception e
     */
    void update(String token, String oldProjectName, String newProjectName) throws Exception;

    /**
     * 删除项目
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
}
