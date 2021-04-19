package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.RelationDao;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import cn.edu.fudan.dependservice.service.RelationService;
import cn.edu.fudan.dependservice.utill.TimeUtill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public RelationData getRelationShips(String ps, String page, String project_names, String repo_uuids, String relation_type, String scan_until, String acs, String order) {
        log.info("ps: "+ ps );
        log.info("project_names: "+ project_names );
        log.info("repo_uuids: "+ repo_uuids );
        log.info("relation_type: "+ relation_type );
        log.info("scan_until: "+ scan_until );
        log.info("scan_until.length: "+ scan_until.length() );
        log.info("acs: "+ acs );
        log.info("order: "+ ps );

        if(scan_until==null||scan_until.length()==0){
            scan_until= TimeUtill.getCurrentDateTime();
        }
        List<RelationView> res=relationDao.getRelationBydate(scan_until);
        if(project_names!=null&&project_names.length()>0){
            List<String> projects= Arrays.asList(project_names.split(","));
            res=res.stream().filter(e->projects.contains(e.getProjectName())).collect(Collectors.toList());
        }
        RelationData relationData =new RelationData();
        int id=0;
        sortRelation(res);
        for(RelationView r:res){
            r.setGroupId(r.getProjectName()+"-"+r.getGroupId());
            r.setId(id++);
        }
        List<RelationView> pageRes=pageRelation(ps,page,res);
        int total =(int)Math.ceil(res.size()*1.0/Integer.valueOf(ps));
        relationData.setRows(pageRes);
        relationData.setRecords(res.size());
        relationData.setPage(Integer.valueOf(page));
        relationData.setTotal(total);
        return relationData;
    }
    public void  sortRelation(List<RelationView> relations){
        relations.sort(new Comparator<RelationView>() {
            @Override
            public int compare(RelationView o1, RelationView o2) {
                if(!o1.getProjectName().equals(o2.getProjectName())){
                    return o1.getProjectName().compareTo(o2.getProjectName());
                }else {
                    return Integer.valueOf(o1.getGroupId())-Integer.valueOf(o2.getGroupId());
                }
            }
        });

    }
    public List<RelationView> pageRelation(String ps, String page,List<RelationView> res){
        List<RelationView> reslist;
        int intpage=Integer.valueOf(page);
        int intps=Integer.valueOf(ps);
        int start=intps*(intpage-1);
        if(start+intps>res.size()){
            reslist=res.subList(start,res.size());
        }else {
            reslist=res.subList(start,start+intps);
        }
        return reslist;
    }
}
