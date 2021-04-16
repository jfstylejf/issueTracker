package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.springframework.stereotype.Service;

import javax.management.relation.Relation;
import java.util.List;

@Service
public interface RelationService {

    List<RelationView> getRelationShips();

}
