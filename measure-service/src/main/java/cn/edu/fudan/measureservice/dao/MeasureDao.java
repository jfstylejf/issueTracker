package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileDetail;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.SneakyThrows;
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
@Repository
public class MeasureDao {

    private MeasureMapper measureMapper;
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
     * 返回所查询库列表下的信息条数
     * @param query 查询条件
     * @return int countNum
     */
    public int getMsgNumByRepo(Query query) {
        return measureMapper.getMsgNumByRepo(query.getRepoUuidList());
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
    public void setProjectMapper(ProjectMapper projectMapper) {this.projectMapper = projectMapper;}

}
