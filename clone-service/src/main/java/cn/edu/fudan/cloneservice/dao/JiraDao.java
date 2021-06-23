package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.mapper.CloneLocationMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import com.alibaba.druid.util.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JiraDao {

    private RepoCommitMapper repoCommitMapper;

    @Autowired
    public void setRepoCommitMapper(RepoCommitMapper repoCommitMapper) {
        this.repoCommitMapper = repoCommitMapper;
    }

    public int getJiraCount(String developer, String status, List<String> repoIdList, String start, String end){
        int sum = 0;
        for(String aRepoId : repoIdList){
            sum += getJiraCountSingle(developer, status, aRepoId, start, end);
        }
        return sum;
    }

    public int getJiraCountSingle(String developer, String status, String repoId, String start, String end){
        if(!StringUtils.isEmpty(repoId)){
            return repoCommitMapper.getJiraCountByDeveloperAndRepoId(developer, status, repoId, start, end);
        }else{
            return 0;
        }
    }
}
