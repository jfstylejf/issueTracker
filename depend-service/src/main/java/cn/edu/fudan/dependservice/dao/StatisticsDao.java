package cn.edu.fudan.dependservice.dao;


import cn.edu.fudan.dependservice.codetrackermapper.FileMapperInCT;
import cn.edu.fudan.dependservice.constants.PublicConstants;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

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
    public void fileMapperInCT(FileMapperInCT fileMapperInCT) {
        this.fileMapperInCT = fileMapperInCT;
    }


    @Autowired
    public void locationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public List<RepoUuidsInfo> getallRepoUuid() {
        List<RepoUuidsInfo> res = new ArrayList<>();
        List<ProjectIdsInfo> projectIdsList = locationMapper.getAllProjectIds();
        for (ProjectIdsInfo projectIdsInfo : projectIdsList) {
            String projectName = locationMapper.getProjectName(projectIdsInfo.getProjectId());
            List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
            for (RepoUuidsInfo re : repoInfo) {
                res.add(re);
            }
        }
        return res;

    }

    public DependencyInfo getDependencyNum(String beginDate, String endDate, String projectId, String showDetail) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
        log.info("projectName: "+projectName);
        log.info("repoinfo.size(): "+repoInfo.size());
        List<RelationShip> relationShips = new ArrayList<>();
        boolean noScan=true;
        for (RepoUuidsInfo repo : repoInfo) {
            log.info("repo: "+repo.getRepoUuid());
            List<Commit> scanedCommit =locationMapper.getScanedCommit(repo.getRepoUuid());

            String latestCommittodate=getLatestCommit(scanedCommit,endDate,repo.getRepoUuid());
            if(latestCommittodate==null){
                continue;
            }
            noScan=false;
            log.info("latestCommitdate"+ latestCommittodate);
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
        System.out.println(res.getProjectName() +" "+ res.getNum());
        return res;
    }

    private String getLatestCommit(List<Commit> scanedCommit, String date, String repouuid) {
        log.info("date: " +date);
        String res=null;
        String latestDate="1000-00-10 00:00:00";
        for(Commit c:scanedCommit){
            log.info("scan time: "+c.getCommitTime());
            if(c.getCommitTime()!=null){
                if(c.getCommitTime().compareTo(date)<0){
                    if(c.getCommitTime().compareTo(latestDate)>0){
                        res=c.getCommitId();
                        latestDate=c.getCommitTime();
                    }
                }
            }
        }
        log.info("latestDate: "+latestDate);
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
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
        List<String> repoList = new ArrayList<>();
        List<RelationShip> relationShips = new ArrayList<>();
        for (RepoUuidsInfo repo : repoInfo) {
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