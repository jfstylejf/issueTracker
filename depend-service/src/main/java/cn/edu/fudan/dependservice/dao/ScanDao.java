package cn.edu.fudan.dependservice.dao;

import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import cn.edu.fudan.dependservice.mapper.ScanMapper;
import cn.edu.fudan.dependservice.utill.TimeUtill;
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

    public int addRelation(RelationShip entity1) {

        int rows = 0;
        rows = relationshipMapper.add(entity1);
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
        rows = groupMapper.add(entity);
        return rows;
    }

}
