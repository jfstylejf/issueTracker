package cn.edu.fudan.dependservice.dao;

import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RelationDao {
    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    GroupMapper groupMapper;
    public List<RelationView>  getRelationsdInGroup(String repoUuid,String commitId,int groupId){
        return relationshipMapper.getRelationsdInGroup(repoUuid, commitId, groupId);
    }


    public  List<RelationView> getRelationBydate(String date){
         return relationshipMapper.getRelationBydate(date);

    }
    public  List<RelationView> getRelationBydateAndProjectIds(String date,String repoUuids){
        return relationshipMapper.getRelationBydateAndProjectIds(date,repoUuids);
    }
}
