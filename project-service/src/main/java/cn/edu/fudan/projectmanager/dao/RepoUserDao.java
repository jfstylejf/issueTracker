package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.RepoUser;
import cn.edu.fudan.projectmanager.mapper.RepoUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author fancying
 */
@Repository
public class RepoUserDao {


    private RepoUserMapper repoUserMapper;

    public void insertRepoUser(RepoUser repoUser){
        repoUserMapper.insertRepoUser(repoUser);
    }

    public Boolean hasRepo(String accountUuid, String url) {
        return repoUserMapper.getRepoCount(accountUuid, url) == 1;
    }

    public void updateRepoName(String accountUuid, String oldName, String newName) {
        repoUserMapper.updateRepoName(accountUuid, oldName, newName);
    }

    public void deleteRelation(String subRepoUuid) {
        repoUserMapper.deleteRelation(subRepoUuid);
    }

    @Autowired
    public void setRepoUserMapper(RepoUserMapper repoUserMapper) {
        this.repoUserMapper = repoUserMapper;
    }
}
