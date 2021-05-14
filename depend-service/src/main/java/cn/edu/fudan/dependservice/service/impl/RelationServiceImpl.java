package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.RelationDao;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.RelationView;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import cn.edu.fudan.dependservice.service.RelationService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelationServiceImpl implements RelationService {
    @Autowired
    RelationDao relationDao;


    private Map<String,String> type_C2E;
    @Autowired
    public void setType_C2E(){
        type_C2E=new HashMap<>();
        type_C2E.put("调用","CALL");
        type_C2E.put("继承","EXTENDS");
        type_C2E.put("实现","IMPLEMENTS");
    }
    private Map<String,String> type_E2C;
    @Autowired
    public void setType_E2C(){
        type_E2C=new HashMap<>();
        type_E2C.put("CALL","调用");
        type_E2C.put("EXTENDS","继承");
        type_E2C.put("IMPLEMENTS","实现");
    }

    @Autowired
    StatisticsDao statisticsDao;

    LocationMapper locationMapper;


    @Autowired
    public void locationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }
    @Override
    public RelationData getRelationShips(String project_names,  String relation_type, String scan_until, String order) {
        if(scan_until==null||scan_until.length()==0){
            scan_until= TimeUtil.getCurrentDateTime();
        }
        List<RelationView> res=relationDao.getRelationBydate(scan_until);
        if(project_names!=null&&project_names.length()>0){
            List<String> projects= Arrays.asList(project_names.split(","));
            res=res.stream().filter(e->projects.contains(e.getProjectName())).collect(Collectors.toList());
        }
        if(relation_type!=null&&relation_type.length()>0){
            List<String> types=Arrays.asList(relation_type.split(",")).stream().map(e->e.toUpperCase()).collect(Collectors.toList());

            res=res.stream().filter(e->{
                for(String s:types){
                    if(e.getRelationType().indexOf(type_C2E.get(s))>=0) return true;
                }
                return false;
            }).collect(Collectors.toList());
        }
        RelationData relationData =new RelationData();
        int id=0;
        sortRelation(res);
        //todo may one project have many repo
        for(RelationView r:res){
            r.setGroupId(r.getProjectName()+"-"+r.getGroupId());
            r.setId(id++);
        }
        relationData.setRows(res);
        relationData.setRecords(res.size());
        relationData.setPage(-1);
        relationData.setTotal(res.size());
        return relationData;
    }

    @Override
    public RelationData getRelationShips(String ps, String page, String project_names,  String relation_type, String scan_until, String order) {
        log.info("projectName: ",project_names);

        if(scan_until==null||scan_until.length()==0){
            scan_until= TimeUtil.getCurrentDateTime();
        }
        List<RelationView> res=relationDao.getRelationBydate(scan_until);
        if(project_names!=null&&project_names.length()>0){
            List<String> projects= Arrays.asList(project_names.split(","));
            res=res.stream().filter(e->projects.contains(e.getProjectName())).collect(Collectors.toList());
        }
        if(relation_type!=null&&relation_type.length()>0){
            List<String> types=Arrays.asList(relation_type.split(",")).stream().map(e->e.toUpperCase()).collect(Collectors.toList());

            res=res.stream().filter(e->{
                for(String s:types){
                    if(e.getRelationType().indexOf(type_C2E.get(s))>=0) return true;
                }
                return false;
            }).collect(Collectors.toList());
        }
        RelationData relationData =new RelationData();
        int id=0;
        sortRelation(res);
        //todo may one project have many repo
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
        for(RelationView r:res){
            for(String s:type_E2C.keySet()){
                String resType= r.getRelationType().replace(s,type_E2C.get(s));
                r.setRelationType(resType);
            }
        }
        return reslist;
    }
}
