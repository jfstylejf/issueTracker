package cn.edu.fudan.dependservice.dao;


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
    private static final String RESPONSE_TOTAL = "total";

    @CacheEvict(value = {"methodList", "fileList", "focusFileNum", "methodDetail"}, allEntries = true, beforeInvocation = true)
    public void clearCache() {

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
                log.info(re.toString());
            }
//

        }
        log.info("getallRepoUuid.size:" + res.size());
        return res;

    }


    public DependencyInfo tempgetDependencyNum(String projectId, String showDetail) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
//        log.info(repoInfo.get(0).getRepoUuid()); one project have to repouuid
        log.info("repoInfo.size():" + repoInfo.size());

        List<String> repoList = new ArrayList<>();
        for (RepoUuidsInfo repo : repoInfo) {
            log.info(repo.getRepoUuid());
            repoList.add(repo.getRepoUuid());
        }

        // get repp 's


        return null;
    }
    public DependencyInfo getDependencyNumbyRepo(String repouuid, String showDetail) {



        List<RelationShip> relationShips = new ArrayList<>();

            relationShips.addAll(locationMapper.getDependencyInfo(repouuid));

        log.info("relationShip.size()" + relationShips.size());
        DependencyInfo res =getDependencyInfoFromRelationShips(relationShips, showDetail);
        return res;


    }

    public DependencyInfo getDependencyNum(String projectId, String showDetail) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
//        log.info(repoInfo.get(0).getRepoUuid()); one project have to repouuid
        log.info("repoInfo.size():" + repoInfo.size());
        List<String> repoList = new ArrayList<>();
        List<RelationShip> relationShips = new ArrayList<>();
        for (RepoUuidsInfo repo : repoInfo) {
            relationShips.addAll(locationMapper.getDependencyInfo(repo.getRepoUuid()));
        }
        log.info("relationShip.size()" + relationShips.size());
        DependencyInfo res=getDependencyInfoFromRelationShips(relationShips, showDetail);
        res.setProjectId(projectId);
        res.setProjectName(projectName);
        return res;
        // get repp 's


    }

    public DependencyInfo getDependencyInfoFromRelationShips(List<RelationShip> relationShips, String showDetail) {
        DependencyInfo res = new DependencyInfo();
        List<String> files = new ArrayList<>();

        for (RelationShip r : relationShips) {
            if (!files.contains(r.getFile())) files.add(r.getFile());
            if (!files.contains(r.getDepend_on())) files.add(r.getDepend_on());
        }
        res.setNum(files.size());
        if (showDetail.equals("true")) {
            List<DependencyDetailInfo> detail=new ArrayList<>();
            for(String file:files){
                //todo get metafile and filename
                detail.add(new DependencyDetailInfo(getFileNameByFilePath(file),file,getMetaFileUuidByFilePath(file)));
            }
            res.setDetail(detail);

        } else {


        }
        return res;

    }

    private String getMetaFileUuidByFilePath(String filePath) {
        return null;

    }

    private String getFileNameByFilePath(String filePath) {
        String[] strings=filePath.split("/");

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