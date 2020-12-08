package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Repository
public class ProjectDao {

    private RestInterfaceManager restInterface;

    private ProjectMapper projectMapper;

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
        if (query.getDeveloper()!=null && !"".equals(query.getDeveloper())) {
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
        }
        return result;
    }

    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {
        this.restInterface = restInterface;
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

}
