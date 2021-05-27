package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.mapper.CloneScanMapper;
import cn.edu.fudan.cloneservice.mapper.RepoMeasureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepoMeasureDao {
    private RepoMeasureMapper repoMeasureMapper;

    @Autowired
    private void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper){
        this.repoMeasureMapper = repoMeasureMapper;
    }

    public int getRepoAddLines(List<String> repoUuidList, String developer, String since, String until){
        int res = repoMeasureMapper.getDeveloperAddLines(repoUuidList, developer, since, until);
        return res;
    }
}
