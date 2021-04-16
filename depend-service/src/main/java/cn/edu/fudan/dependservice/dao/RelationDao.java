package cn.edu.fudan.dependservice.dao;

import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RelationDao {
    @Autowired
    RelationshipMapper relationshipMapper;
     public  List<RelationView> getpageRelation(int ps,int page){
        return relationshipMapper.getAllRelationships(ps);
    }
    public  List<RelationView> getRelationBydate(String date){
         return relationshipMapper.getRelationBydate(date);

    }
}
