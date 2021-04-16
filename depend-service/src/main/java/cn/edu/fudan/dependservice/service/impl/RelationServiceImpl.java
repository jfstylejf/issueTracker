package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.RelationDao;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import cn.edu.fudan.dependservice.service.RelationService;
import cn.edu.fudan.dependservice.utill.TimeUtill;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Slf4j
@Service
public class RelationServiceImpl implements RelationService {
    @Autowired
    RelationDao relationDao;

    @Autowired
    StatisticsDao statisticsDao;

    LocationMapper locationMapper;


    @Autowired
    public void locationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    @Override
    public RelationData getRelationShips() {
        RelationData relationData =new RelationData();
        List<RelationView> res=relationDao.getpageRelation(10,1);
        int id=0;
        for(RelationView r:res){
            r.setGroupId(r.getProjectName()+"-"+r.getGroupId());
            r.setId(id++);
        }
        relationData.setRows(res);
        relationData.setRecords(1000);
        relationData.setPage(1);
        relationData.setTotal(100);
        return relationData;
    }

    @Override
    public RelationData getRelationShips(String ps, String page, String project_names, String repo_uuids, String relation_type, String scan_until, String acs, String order) {
        log.info("ps: "+ ps );
        log.info("project_names: "+ project_names );
        log.info("repo_uuids: "+ repo_uuids );
        log.info("relation_type: "+ relation_type );
        log.info("scan_until: "+ scan_until );
        log.info("acs: "+ acs );
        log.info("order: "+ ps );
        if(scan_until==null){
            scan_until= TimeUtill.getCurrentDateTime();
        }
        if(order==null){
        }
        List<RelationView> res=relationDao.getRelationBydate(scan_until);
        log.info("relations.size():+ "+res.size());
        RelationData relationData =new RelationData();
        int id=0;
        for(RelationView r:res){
            r.setGroupId(r.getProjectName()+"-"+r.getGroupId());
            r.setId(id++);
        }
        List<RelationView> pageRes=pageRelation(ps,page,res);
        log.info("pageRes.size(): "+pageRes.size());
        int total =(int)Math.ceil(res.size()*1.0/Integer.valueOf(ps));
        relationData.setRows(pageRes);
        relationData.setRecords(res.size());
        relationData.setPage(Integer.valueOf(page));
        relationData.setTotal(total);
        return relationData;
    }
    public List<RelationView> pageRelation(String ps, String page,List<RelationView> res){
        int intpage=Integer.valueOf(page);
        int intps=Integer.valueOf(ps);

        int start=intps*(intpage-1);
        res.sort(new Comparator<RelationView>() {
            @Override
            public int compare(RelationView o1, RelationView o2) {
               return  o1.getGroupId().compareTo(o2.getGroupId());
            }
        });
        if(start+intps>res.size()){
            return res.subList(start,res.size());
        }
        return res.subList(start,start+intps);
    }
}
