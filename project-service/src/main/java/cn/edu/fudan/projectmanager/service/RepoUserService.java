package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 代码库与项目和人之间的关系
 *
 * @author fancying
 * create: 2020-10-04 14:29
 **/
@Slf4j
@Service
public class RepoUserService {

    private SubRepositoryDao subRepository;
    private AccountMapper accountMapper;

    /**
     * @return  k projectName v: list [k: repo_id, name]
     */
    public Map<String, List<Map<String, String>>> getProjectAndRepoRelation(int recycled) {
        // key project_name,name,sub_repository_uuid,recycled
        List<Map<String, Object>> projects =  subRepository.getAllProjectRepoRelation();

        boolean isAll = recycled == SubRepository.ALL;

        Map<String, List< Map<String, String>>> result = new HashMap<>(8);
        for (Map<String, Object> project : projects) {

            int recycledStatus = (int)project.get("recycled");
            if (!isAll && recycled != recycledStatus){
                continue;
            }

            String projectName = (String) project.get("project_name");
            if (StringUtils.isEmpty(projectName)) {
                projectName = "unnamed";
            }
            if (! result.keySet().contains(projectName)) {
                result.put(projectName, new ArrayList<>(4));
            }
            List< Map<String, String>> v = result.get(projectName);
            Map<String, String> p = new HashMap<>(4);
            p.put("repo_id", (String)project.get("repo_uuid"));
            p.put("name", (String)project.get("name"));
            v.add(p);
        }
        return result;
    }


    public SubRepository getProjectInfoByRepoId(String repoUuid) {
        return subRepository.getSubRepoByRepoUuid(repoUuid);
    }

    public String getRepoUuid(String projectUuid) {
        return subRepository.getSubRepoByUuid(projectUuid).getRepoUuid();
    }

    @Autowired
    public void setSubRepository(SubRepositoryDao subRepository) {
        this.subRepository = subRepository;
    }

    public List<SubRepository> getRepositoryByAccountUuid(String accountUuid) {
        return subRepository.getAllSubRepoByAccountUuid(accountUuid);
    }

    public List<Map<String, Object>> getProjectInfoByAccountName(String accountName) {
        return accountMapper.getProjectInfoByAccountName(accountName);
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}