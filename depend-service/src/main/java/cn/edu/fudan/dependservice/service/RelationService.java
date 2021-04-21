package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.springframework.stereotype.Service;

import javax.management.relation.Relation;
import java.util.List;

@Service
public interface RelationService {

    RelationData getRelationShips();

    RelationData getRelationShips(String ps, String page, String project_names, String repo_uuids, String relation_type, String scan_until, String acs, String order);
}
