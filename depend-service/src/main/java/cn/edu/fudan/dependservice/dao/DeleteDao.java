package cn.edu.fudan.dependservice.dao;

import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-11 11:29
 **/
@Repository
public class DeleteDao {
    @Autowired
    RelationshipMapper relationshipMapper;
    @Autowired
    GroupMapper groupMapper;
    public boolean norepoData(String repouuid){
        return  relationshipMapper.getCountByRepoUuid(repouuid)==0&&
                groupMapper.getCountByRepoUuid(repouuid)==0;

    }
    public void delete(String repouuid){
        while (relationshipMapper.getCountByRepoUuid(repouuid)>0){
            relationshipMapper.deleteByRepoUuidLimit100(repouuid);

        }
        while (groupMapper.getCountByRepoUuid(repouuid)>0){
            groupMapper.deleteByRepoUuidLimit100(repouuid);

        }

    }
}
