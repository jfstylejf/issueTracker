package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.mapper.SubRepositoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fancying
 */
@Repository
public class SubRepositoryDao {

    private SubRepositoryMapper subRepositoryMapper;

    public Integer insertOneRepo(SubRepository subRepository){
        return subRepositoryMapper.insertOneRepo(subRepository);
    }

    public void updateRepository(SubRepository subRepository){
        subRepositoryMapper.updateSubRepository(subRepository);
    }

    public SubRepository getSubRepoByUuid(String uuid){
        return subRepositoryMapper.getSubRepoByUuid(uuid);
    }

    public Date getLatestCommitTime(String repoId){
        return subRepositoryMapper.getLatestCommitTime(repoId);
    }

    public void setRecycled(String subRepoUuid) {
        subRepositoryMapper.setRecycled(subRepoUuid);
    }

    public void deleteRepo(String subRepoUuid) {
        subRepositoryMapper.deleteRepo(subRepoUuid);
    }

    public List<SubRepository> getAllSubRepoByAccountUuid(String accountUuid) {
        return subRepositoryMapper.getAllSubRepoByAccountId(accountUuid);
    }

    public List<Map<String, Object>> getAllProjectRepoRelation(){
        return subRepositoryMapper.getAllProjectRepoRelation();
    }

    @Autowired
    public void setSubRepositoryMapper(SubRepositoryMapper subRepositoryMapper) {
        this.subRepositoryMapper = subRepositoryMapper;
    }

    public SubRepository getSubRepoByRepoUuid(String repoUuid) {
        return subRepositoryMapper.getSubRepoByRepoUuid(repoUuid);
    }

}
