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

    public List<RepoUuidsInfo> getallRepoUuid(){
        List<RepoUuidsInfo>res=new ArrayList<>();
        List<ProjectIdsInfo> projectIdsList = locationMapper.getAllProjectIds();
        for(ProjectIdsInfo projectIdsInfo:projectIdsList){
            String projectName = locationMapper.getProjectName(projectIdsInfo.getProjectId());
            List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
            for(RepoUuidsInfo re:repoInfo){
                res.add(re);
                log.info(re.toString());
            }
//

        }
        log.info("getallRepoUuid.size:"+res.size());
        return res;

    }




    public DependencyInfo getDependencyNum(String beginDate, String endDate, String projectId, String showDetail, String level) {
        String projectName = locationMapper.getProjectName(projectId);
        List<RepoUuidsInfo> repoInfo = locationMapper.getRepoUuids(projectName);
//        log.info(repoInfo.get(0).getRepoUuid()); one project have to repouuid
        log.info("repoInfo.size():"+repoInfo.size());

        List<String> repoList = new ArrayList<>();
        for (RepoUuidsInfo repo : repoInfo) {
            log.info(repo.getRepoUuid());
            repoList.add(repo.getRepoUuid());
        }
        // get repp 's


        return null;
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