package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.RelationDao;
import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RelationServiceImpl implements RelationService {
    @Autowired
    RelationDao relationDao;
    @Override
    public List<RelationView> getRelationShips() {
        List<RelationView> res=relationDao.getpageRelation(10,1);
        int id=0;
        for(RelationView r:res){
            r.setGroupId(r.getProjectName()+"-"+r.getGroupId());
            r.setId(id++);
        }
        return res;
    }
}
