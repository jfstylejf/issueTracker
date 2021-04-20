package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipMapper {
    // todo get certain commit  use where commit in
    public List<RelationView> getAllRelationships(int ps);
    public List<RelationView> getRelationBydate(String date);

    public int add(RelationShip relationship);


}
