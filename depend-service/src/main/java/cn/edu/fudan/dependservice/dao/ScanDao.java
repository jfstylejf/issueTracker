package cn.edu.fudan.dependservice.dao;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import cn.edu.fudan.dependservice.mapper.RepoMapper;
import cn.edu.fudan.dependservice.mapper.ScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ScanDao {


    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    ScanMapper scanMapper;

    @Autowired
    RepoMapper repoMapper;

    public String getRepoLanguage(String repoUuid){
        return repoMapper.getLanguage(repoUuid);
    }

    public void clearLastScan(ScanRepo scanRepo){
        relationshipMapper.deleteByRepoUuidAndCommitId(scanRepo.getRepoUuid(),scanRepo.getScanCommit());
    }
    public int addRelation(RelationShip relationShip) {
        // todo clear relation
        int rows = 0;
        rows = relationshipMapper.add(relationShip);
        return rows;
    }
    public int updateScan(ScanRepo scanRepo){
        //find if have
        // todo add
        scanMapper.insert(scanRepo);
        return 1;

    }
    public ScanStatus getScanStatus(String repouuid){
         return  scanMapper.getScanStatus(repouuid);
    }

    public int addGroup(Group entity) {

        int rows = 0;
        // todo now do not need group
//        rows = groupMapper.add(entity);
        return rows;
    }

}
