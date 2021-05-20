package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileDetail;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class MeasureDao {

    private MeasureMapper measureMapper;
    private RepoMeasureMapper repoMeasureMapper;
    private FileMeasureMapper fileMeasureMapper;
    private ProjectMapper projectMapper;


    public DeveloperWorkLoad getDeveloperWorkLoadData(Query query) {
        return measureMapper.getDeveloperWorkLoad(query.getRepoUuidList(),query.getDeveloper(),query.getSince(),query.getUntil());
    }


    /**
     * 获取相应的commit提交次数数据
     * @param query 查询条件
     * @return Map<String,Object> developerCommitCount,totalCommitCount
     */
    public Map<String,Object> getCommitCountsByDuration(Query query) {
        Map<String,Object> map = new HashMap<>(10);
        int developerCommitCount = projectMapper.getDeveloperCommitCountsByDuration(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
        int totalCommitCount = projectMapper.getDeveloperCommitCountsByDuration(query.getRepoUuidList(),query.getSince(),query.getUntil(),null);
        map.put("developerCommitCount",developerCommitCount);
        map.put("totalCommitCount",totalCommitCount);
        return map;
    }

    /**
     * 获取所查询库列表中前3名增加代码物理行数的开发者
     * @param query 查询条件
     * @return key : developerName , developerLoc
     */
    public List<Map<String,Object>> getDeveloperRankByLoc(Query query) {
        return measureMapper.getDeveloperRankByLoc(query.getRepoUuidList(),query.getSince(),query.getUntil());
    }


    /**
     * 查询库下所有大文件最新信息
     * @param repoUuidList 查询库列表
     * @return new ArrayList<{@link ProjectBigFileDetail}>
     */
    public List<ProjectBigFileDetail> getCurrentBigFileInfo(List<String> repoUuidList,String until) {
        List<ProjectBigFileDetail> projectBigFileDetailList = new ArrayList<>();
        List<Map<String,Object>> currentFileInfoByRepoUuidList = fileMeasureMapper.getCurrentFileInfoByRepoUuidList(repoUuidList,until);
        for (Map<String,Object> map : currentFileInfoByRepoUuidList) {
            ProjectBigFileDetail projectBigFileDetail = new ProjectBigFileDetail();
            String filePath = (String) map.get("file_path");
            int totalLines = (int) map.get("total_lines");
            Timestamp currentModifyTime = (Timestamp) map.get("currentModifyTime");
            LocalDate temp = currentModifyTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String repoUuid = (String) map.get("repo_id");
            projectBigFileDetail.setCurrentLines(totalLines);
            projectBigFileDetail.setFilePath(filePath);
            projectBigFileDetail.setRepoUuid(repoUuid);
            projectBigFileDetail.setCurrentModifyTime(temp.format(DateTimeUtil.dtf));
            projectBigFileDetailList.add(projectBigFileDetail);
        }
        return projectBigFileDetailList;
    }


    /**
     * 删除所属repo下repo_measure,file_measure表数据
     * @param repoUuid 待删除库
     */
    public boolean deleteRepoMsg(String repoUuid) {
        return deleteRepoMeasureMsgByRepo(repoUuid) && deleteFileMeasureMsgByRepo(repoUuid);
    }

    /**
     * 删除所属repo下repo_measure表数据
     * @param repoUuid 待删除库
     * @return true : 删除成功 ， false : 删除失败
     */
    public boolean deleteRepoMeasureMsgByRepo(String repoUuid) {
        int countNum = repoMeasureMapper.getRepoMeasureMsgNumByRepo(repoUuid);
        try {
            while (countNum > 0) {
                countNum -= 500;
                repoMeasureMapper.deleteRepoMeasureMsg(repoUuid);
            }
            log.info("delete repoMsg from repo_measure Success!");
            return true;
        }catch (Exception e) {
            e.getMessage();
            log.error("delete repoMsg from repo_measure Failed");
        }
        return false;
    }

    /**
     * 删除所属repo下file_measure表数据
     * @param repoUuid 待删除库
     * @return true : 删除成功 ， false : 删除失败
     */
    public boolean deleteFileMeasureMsgByRepo(String repoUuid) {
        int countNum = fileMeasureMapper.getFileMeasureMsgNumByRepo(repoUuid);
        try {
            while (countNum > 0) {
                countNum -= 500;
                fileMeasureMapper.deleteFileMeasureMsg(repoUuid);
            }
            log.info("delete repoMsg from file_measure Success!");
            return true;
        }catch (Exception e) {
            e.getMessage();
            log.error("delete repoMsg from file_measure Failed");
        }
        return false;
    }



    @SneakyThrows
    public int getDeveloperDiffCcn(String repoUuid,String since,String until,String developer) {
        Objects.requireNonNull(developer,"开发者不可为空");
        return fileMeasureMapper.getDeveloperDiffCcn(repoUuid,developer,since,until);
    }


    @Autowired
    public void setMeasureMapper(MeasureMapper measureMapper){
        this.measureMapper = measureMapper;
    }

    @Autowired
    public void setFileMeasureMapper(FileMeasureMapper fileMeasureMapper) {
        this.fileMeasureMapper = fileMeasureMapper;
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {this.projectMapper = projectMapper;}

}
