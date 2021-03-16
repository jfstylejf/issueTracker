package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.RelationShip;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipMapper {
    public List<RelationShip> getAllRelationships();

    public int add(RelationShip relationship);


}
