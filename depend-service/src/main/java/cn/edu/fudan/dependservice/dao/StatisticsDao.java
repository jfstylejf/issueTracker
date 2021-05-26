package cn.edu.fudan.dependservice.dao;


import cn.edu.fudan.dependservice.codetrackermapper.FileMapperInCT;
import cn.edu.fudan.dependservice.constants.PublicConstants;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 * create 2019-11-12 09:59
 **/
@Slf4j
@Repository
public class StatisticsDao implements PublicConstants {
    private LocationMapper locationMapper;
    private FileMapperInCT fileMapperInCT;

    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    public void fileMapperInCT(FileMapperInCT fileMapperInCT) {
        this.fileMapperInCT = fileMapperInCT;
    }


    @Autowired
    public void locationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public List<RepoInfo> getallRepoUuid() {
        List<RepoInfo> res = new ArrayList<>();
        List<ProjectIdsInfo> projectIdsList = locationMapper.getAllProjectIds();
        for (ProjectIdsInfo projectIdsInfo : projectIdsList) {
            String projectName = locationMapper.getProjectName(projectIdsInfo.getProjectId());
            List<RepoInfo> repoInfo = locationMapper.getRepoUuids(projectName);
            for (RepoInfo re : repoInfo) {
                res.add(re);
            }
        }
        return res;

    }
    //todo  Query the database once
    public List<DependencyInfo> getDependencyNum2(String endDate, String projectIds, String showDetail) {
        Map<String,DependencyInfo> resMap =new HashMap<>();
        for(String s:projectIds.split(",")){
            resMap.put(s,null);
        }
        List<DependencyInfo> res=new ArrayList<>();
        List<Project> projectList = locationMapper.getAllProjects();
        Map<String,String> projectNameToId =new HashMap<>();
        Map<String,String> projectIdToName =new HashMap<>();
        for(Project p:projectList){
            projectNameToId.put(p.getProjectName(),p.getProjectId());
            projectIdToName.put(p.getProjectId(),p.getProjectName());
        }
        List<RelationView> relationViews = relationshipMapper.getRelationBydate(endDate);
        Map<String,Set<File>> map =new HashMap<>();
        for(RelationView r:relationViews){
            File source = new File(r.getSourceFile(), r.getRepoUuid(), r.getCommit_id());
            File target = new File(r.getTargetFile(), r.getRepoUuid(), r.getCommit_id());
            Set<File> set =map.getOrDefault(r.getProjectName(),new HashSet<File>());
            set.add(source);
            set.add(target);
            map.put(r.getProjectName(),set);
        }
        for(Map.Entry<String,Set<File>> m:map.entrySet()){
            String prjectName=m.getKey();
            String prjectId =projectNameToId.get(prjectName);
            if(resMap.keySet().contains(prjectId)){
                DependencyInfo dependencyInfo = new DependencyInfo();
                dependencyInfo.setProjectName(m.getKey());
                dependencyInfo.setNum(m.getValue().size());
                dependencyInfo.setDate(endDate.split(" ")[0]);
                dependencyInfo.setProjectId(projectNameToId.get(m.getKey()));
                resMap.put(projectNameToId.get(dependencyInfo.getProjectName()),dependencyInfo);
            }
        }
        for(Map.Entry<String,DependencyInfo> m:resMap.entrySet()){
            String projectId =m.getKey();
            if(m.getValue()==null){
                res.add(getZeroOrNoScan(projectId,projectIdToName.get(projectId),endDate));
            }else {
                res.add(m.getValue());
            }

        }
        return res;
    }
    public DependencyInfo getZeroOrNoScan(String projectId,String projectName,String endDate){
        List<RepoInfo> repoInfo = locationMapper.getRepoUuids(projectName);
        boolean noScan=true;
        for (RepoInfo repo : repoInfo) {
            List<Commit> scanedCommit =locationMapper.getScanedCommit(repo.getRepoUuid());
            String latestCommittodate=getLatestCommit(scanedCommit,endDate);
            if(latestCommittodate==null){
                continue;
            }
            noScan=false;
        }
        if(noScan){
            DependencyInfo res=new DependencyInfo();
            res.setProjectId(projectId);
            res.setProjectName(projectName);
            res.setNum(null);
            res.setDate(endDate.split(" ")[0]);
            return res;
        }
        DependencyInfo res=new DependencyInfo();
        res.setProjectId(projectId);
        res.setProjectName(projectName);
        res.setNum(0);
        res.setDate(endDate.split(" ")[0]);
        return res;
    }

    public DependencyInfo getDependencyNum(String beginDate, String endDate, String projectId, String showDetail) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoInfo> repoInfo = locationMapper.getRepoUuids(projectName);
        //  to do
        repoInfo=repoInfo.stream().filter((e)->e.getRepoUuid()!=null).collect(Collectors.toList());
        List<RelationShip> relationShips = new ArrayList<>();
        boolean noScan=true;
//        log.info("projectId: {}",projectId);
        for (RepoInfo repo : repoInfo) {
//            System.out.println(" repoName : "+repo.getRepoName());
//            log.info("repouuid: {}",repo.getRepoUuid());
            List<Commit> scanedCommit =locationMapper.getScanedCommit(repo.getRepoUuid());
//            log.info("scanedCommit.size(): {}",scanedCommit.size());
            String latestCommittodate=getLatestCommit(scanedCommit,endDate);
            if(latestCommittodate==null){
                continue;
            }
            noScan=false;
            relationShips.addAll(locationMapper.getFileByCommitId(repo.getRepoUuid(),latestCommittodate));
        }
        if(noScan){
            DependencyInfo res=new DependencyInfo();
            res.setProjectId(projectId);
            res.setProjectName(projectName);
            res.setNum(null);
            res.setDate(endDate.split(" ")[0]);
            return res;
        }
        if(relationShips.size()==0){
            DependencyInfo res=new DependencyInfo();
            res.setProjectId(projectId);
            res.setProjectName(projectName);
            res.setNum(0);
            res.setDate(endDate.split(" ")[0]);
            return res;
        }
        DependencyInfo res = getDependencyInfoFromRelationShips(relationShips, showDetail);
        res.setProjectId(projectId);
        res.setProjectName(projectName);
        res.setDate(endDate.split(" ")[0]);
        return res;
    }

    private String getLatestCommit(List<Commit> scanedCommit, String date) {
        String res=null;
        String latestDate="1000-00-10 00:00:00";
        for(Commit c:scanedCommit){
            if(c.getCommitTime()!=null){
                if(c.getCommitTime().compareTo(date)<0){
                    if(c.getCommitTime().compareTo(latestDate)>0){
                        res=c.getCommitId();
                        latestDate=c.getCommitTime();
                    }
                }
            }
        }
        return res;
    }


    public DependencyInfo getDependencyInfoFromRelationShips(List<RelationShip> relationShips, String showDetail) {
        DependencyInfo res = new DependencyInfo();
        List<File> files = new ArrayList<>();
        //todo target will be null
        for (RelationShip r : relationShips) {
            File source = new File(r.getFile(), r.getRepo_uuid(), r.getCommit_id());
            if(r.getDepend_on()!=null&&r.getDepend_on()!="null"){
                File target = new File(r.getDepend_on(), r.getRepo_uuid(), r.getCommit_id());
                if (!files.contains(target)) files.add(target);
            }
            if (!files.contains(source)) files.add(source);

        }
        res.setNum(files.size());
        if (showDetail.equals("true")) {
            List<DependencyDetailInfo> detail = new ArrayList<>();
            for (File file : files) {
                //todo get metafile and filename
                detail.add(new DependencyDetailInfo(getFileNameByFilePath(file), file.getFileName(), getMetaFileUuidByFilePath(file)));
            }
            res.setDetail(detail);

        } else {


        }
        return res;

    }

    private String getMetaFileUuidByFilePath(File file) {
        String filePathforSQL = file.getFileName().split("/", 3)[2];

        log.info(filePathforSQL);
        log.info(file.getCommitId());
        log.info(file.getRepoUUid());
        return fileMapperInCT.getMetaFileUUid(filePathforSQL, file.getRepoUUid(), file.getCommitId());
    }

    private String getFileNameByFilePath(File file) {
        String[] strings = file.getFileName().split("/");

        return strings[strings.length - 1];
    }

    public String getAllProjectIds() {
        List<ProjectIdsInfo> projectIdsList = locationMapper.getAllProjectIds();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < projectIdsList.size(); i++) {
            sb.append(projectIdsList.get(i).getProjectId()).append(",");
        }
        return sb.substring(0, sb.toString().length() - 1);
    }

    public List<DependencyInfo> getdependencyList(List<RelationShip> relationShips,String repouuid) {
        List<DependencyInfo> ress = new ArrayList<>();
        Map<String, List<String>> map = new HashMap<>();
        for (RelationShip r : relationShips) {
            if (map.containsKey(r.getCommit_id())) {
                List thislist = map.get(r.getCommit_id());
                if (!thislist.contains(r.getFile())) {
                    thislist.add(r.getFile());
                }
                if (!thislist.contains(r.getDepend_on())) {
                    thislist.add(r.getDepend_on());
                }
                map.put(r.getCommit_id(), thislist);
            } else {
                List<String> thislist = new ArrayList();
                thislist.add(r.getFile());
                thislist.add(r.getDepend_on());
                map.put(r.getCommit_id(), thislist);
            }
        }
        log.info("map.size(): " + map.size());
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            DependencyInfo dependencyInfo = new DependencyInfo();
            dependencyInfo.setNum(entry.getValue().size());
            // get date by commit id

            dependencyInfo.setDate(null);
            ress.add(dependencyInfo);
        }

        return ress;

    }
    public List<DependencyInfo> getNumifHaveCommit(String projectId) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoInfo> repoInfo = locationMapper.getRepoUuids(projectName);
        List<String> repoList = new ArrayList<>();
        List<RelationShip> relationShips = new ArrayList<>();
        for (RepoInfo repo : repoInfo) {
            relationShips.addAll(locationMapper.getDependencyInfo(repo.getRepoUuid()));
        }
        List<DependencyInfo> res = getdependencyList(relationShips,null);
        res.forEach((e) -> {
            e.setProjectId(projectId);
            e.setProjectName(projectName);
        });
        log.info("project:" + projectId + "have: " + res.size() + "  result");
        return res;
    }


}