package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipMapper {
    public List<RelationView> getAllRelationships(int ps);

    public int add(RelationShip relationship);


}
