package cn.edu.fudan.dependservice.dao;


import cn.edu.fudan.dependservice.codetrackermapper.FileMapperInCT;
import cn.edu.fudan.dependservice.constants.PublicConstants;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private static final String RESPONSE_TOTAL = "total";

    @CacheEvict(value = {"methodList", "fileList", "focusFileNum", "methodDetail"}, allEntries = true, beforeInvocation = true)

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

    public DependencyInfo getDependencyNumbyRepo(String repouuid, String showDetail) {



        List<RelationShip> relationShips = new ArrayList<>();

            relationShips.addAll(locationMapper.getDependencyInfo(repouuid));

        DependencyInfo res =getDependencyInfoFromRelationShips(relationShips, showDetail);
        return res;


    }

//    public DependencyInfo getDependencyNum(String projectId, String showDetail) {
//        String projectName = locationMapper.getProjectName(projectId);
//        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
////        log.info(repoInfo.get(0).getRepoUuid()); one project have to repouuid
//        log.info("repoInfo.size():" + repoInfo.size());
//        List<String> repoList = new ArrayList<>();
//        List<RelationShip> relationShips = new ArrayList<>();
//        for (RepoUuidsInfo repo : repoInfo) {
//            relationShips.addAll(locationMapper.getDependencyInfo(repo.getRepoUuid()));
//        }
//        log.info("relationShip.size()" + relationShips.size());
//        DependencyInfo res=getDependencyInfoFromRelationShips(relationShips, showDetail);
//        res.setProjectId(projectId);
//        res.setProjectName(projectName);
//        return res;
//        // get repp 's
//
//
//    }
    public DependencyInfo getDependencyNum(String beginDate, String endDate,String projectId, String showDetail) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
//        log.info(repoInfo.get(0).getRepoUuid()); one project have to repouuid
        List<String> repoList = new ArrayList<>();
        List<RelationShip> relationShips = new ArrayList<>();
        for (RepoUuidsInfo repo : repoInfo) {
            relationShips.addAll(locationMapper.getDependencyInfo(repo.getRepoUuid()));
        }
        DependencyInfo res=getDependencyInfoFromRelationShips(relationShips, showDetail);
        res.setProjectId(projectId);
        res.setProjectName(projectName);
        res.setDate(endDate.split(" ")[0]);
        return res;
        // get repp 's


    }

    public DependencyInfo getDependencyInfoFromRelationShips(List<RelationShip> relationShips, String showDetail) {
        DependencyInfo res = new DependencyInfo();
        List<File> files = new ArrayList<>();

        for (RelationShip r : relationShips) {
            File source= new File(r.getFile(),r.getRepo_uuid(),r.getCommit_id());
            File target= new File(r.getDepend_on(),r.getRepo_uuid(),r.getCommit_id());
            if (!files.contains(source)) files.add(source);
            if (!files.contains(target)) files.add(target);
        }
        res.setNum(files.size());
        if (showDetail.equals("true")) {
            List<DependencyDetailInfo> detail=new ArrayList<>();
            for(File file:files){
                //todo get metafile and filename
                detail.add(new DependencyDetailInfo(getFileNameByFilePath(file),file.getFileName(),getMetaFileUuidByFilePath(file)));
            }
            res.setDetail(detail);

        } else {


        }
        return res;

    }

    private String getMetaFileUuidByFilePath(File file) {
        String filePathforSQL=file.getFileName().split("/",3)[2];

        log.info(filePathforSQL);
        log.info(file.getCommitId());
        log.info(file.getRepoUUid());
        return fileMapperInCT.getMetaFileUUid(filePathforSQL,file.getRepoUUid(),file.getCommitId());
//        return fileMapperInCT.getLastedScannedCommit(filePathforSQL,file.getRepoUUid(),file.getCommitId());
//        return filePathforSQL;

    }

    private String getFileNameByFilePath(File file) {
        String[] strings=file.getFileName().split("/");

        return strings[strings.length-1];
    }


    public String getAllProjectIds() {
        List<ProjectIdsInfo> projectIdsList = locationMapper.getAllProjectIds();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < projectIdsList.size(); i++) {
            sb.append(projectIdsList.get(i).getProjectId()).append(",");
        }
        return sb.substring(0, sb.toString().length() - 1);
    }

}