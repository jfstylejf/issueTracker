package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;
import cn.edu.fudan.measureservice.domain.metric.RepoTagMetric;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileDetail;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
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
        for (String repoUuid : repoUuidList) {
            List<ProjectBigFileDetail> projectBigFileDetails = ((MeasureDao) AopContext.currentProxy()).getCurrentBigFileInfo(repoUuid,until);
            projectBigFileDetailList.addAll(projectBigFileDetails);
        }
        return projectBigFileDetailList;
    }


    /**
     * 查询库下所有大文件最新信息
     * @param repoUuid 查询库
     * @return new ArrayList<{@link ProjectBigFileDetail}>
     */
    @Cacheable(value = "repoBigFileInfo",key = "#repoUuid",condition = "#until == null ")
    public List<ProjectBigFileDetail> getCurrentBigFileInfo(String repoUuid, String until) {
        log.info("get Current big file in {} til {}",repoUuid,until);
        Objects.requireNonNull(repoUuid);
        List<ProjectBigFileDetail> projectBigFileDetailList = new ArrayList<>();
        List<Map<String,Object>> currentFileInfoByRepoUuidList = fileMeasureMapper.getCurrentFileInfoByRepoUuidList(Collections.singletonList(repoUuid),until);
        for (Map<String,Object> map : currentFileInfoByRepoUuidList) {
            ProjectBigFileDetail projectBigFileDetail = new ProjectBigFileDetail();
            String filePath = (String) map.get("file_path");
            int totalLines = (int) map.get("absolute_lines");
            Timestamp currentModifyTime = (Timestamp) map.get("currentModifyTime");
            LocalDate temp = currentModifyTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            projectBigFileDetail.setCurrentLines(totalLines);
            projectBigFileDetail.setFilePath(filePath);
            projectBigFileDetail.setRepoUuid(repoUuid);
            projectBigFileDetail.setCurrentModifyTime(temp.format(DateTimeUtil.dtf));
            projectBigFileDetailList.add(projectBigFileDetail);
        }
        return projectBigFileDetailList;
    }


    public List<Map<String,Object>> getProjectValidJiraCommitMsg(Query query) {
        return repoMeasureMapper.getProjectValidJiraCommitMsg(query.getDeveloper(),query.getRepoUuidList(),query.getSince(),query.getUntil());
    }

    /**
     * 获取项目包含库的合法提交信息（去除Merge）
     * @param query 查询条件
     * @return List<Map<String,Object>> key : repo_id, developer , repo_id, commit_time , commit_id , message , is_compliance
     */
    public List<Map<String,Object>> getProjectValidCommitMsg(Query query) {
        return repoMeasureMapper.getProjectValidCommitMsg(query.getDeveloper(),query.getRepoUuidList(),query.getSince(),query.getUntil());
    }

    /**
     * 分页获取项目包括库的合法提交明细（去除Merge）
     * @param query 查询条件
     * @param beginIndex 查询起始位置
     * @param ps 每页大小
     * @return 分页查询后合法提交明细 key : developer , repo_id, commit_time , commit_id , message
     */
    public List<Map<String,Object>> getProjectValidCommitMsg(Query query,int beginIndex, int ps) {
        return repoMeasureMapper.getProjectValidCommitMsgWithPage(query.getDeveloper(),query.getRepoUuidList(),query.getSince(),query.getUntil(),ps,beginIndex);
    }

    /**
     * 分页获取项目包括库的合法提交 JIRA 明细（去除Merge）
     * @param query 查询条件
     * @param beginIndex 查询起始位置
     * @param ps 每页大小
     * @return 分页查询后合法提交明细 key : developer , repo_id, commit_time , commit_id , message
     */
    public List<Map<String,Object>> getProjectValidJiraCommitMsg(Query query,int beginIndex, int ps) {
        return repoMeasureMapper.getProjectValidJiraCommitMsgWithPage(query.getDeveloper(),query.getRepoUuidList(),query.getSince(),query.getUntil(),ps,beginIndex);
    }

    /**
     * 分页获取项目包括库的合法提交 非JIRA 明细（去除Merge）
     * @param query 查询条件
     * @param beginIndex 查询起始位置
     * @param ps 每页大小
     * @return 分页查询后合法提交明细 key : developer , repo_id, commit_time , commit_id , message
     */
    public List<Map<String,Object>> getProjectValidNotJiraCommitMsg(Query query,int beginIndex, int ps) {
        return repoMeasureMapper.getProjectValidNotJiraCommitMsgWithPage(query.getDeveloper(),query.getRepoUuidList(),query.getSince(),query.getUntil(),ps,beginIndex);
    }


    @CacheEvict(value = "repoBigFileInfo", allEntries=true, beforeInvocation = true)
    public void deleteRepoBigFileTrendChart() {

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

    /**
     * 查询对应 repoUuid 下的库维度基线， 若存在维度对应的 repo, 则获取该自定义基线数据， 否则取初始化基线数据
     * @param repoUuid 查询库
     * @return 该库的各维度基线
     */
    public List<RepoTagMetric> getRepoMetric(String repoUuid) {
        List<RepoTagMetric> repoTagMetricList = new ArrayList<>();
        // 用来判断该库下哪些维度尚未更新
        List<String> isValid = new ArrayList<>();
        try {
            List<RepoTagMetric> initialTagMetricList = repoMeasureMapper.getRepoTagMetricList(null);
            if (repoUuid != null) {
                List<RepoTagMetric> tempList = repoMeasureMapper.getRepoTagMetricList(repoUuid);
                for (RepoTagMetric repoTagMetric : tempList) {
                    isValid.add(repoTagMetric.getTag());
                    repoTagMetricList.add(repoTagMetric);
                }
            }
            for (RepoTagMetric repoTagMetric : initialTagMetricList) {
                String tag = repoTagMetric.getTag();
                if (isValid.contains(tag)) {
                    continue;
                }
                repoTagMetricList.add(repoTagMetric);
            }
            log.info("get RepoMetric in {} success\n",repoUuid);
            return repoTagMetricList;
        }catch (Exception e) {
            e.getMessage();
            log.error("get RepoMetric in {} failed\n",repoUuid);
            return null;
        }
    }

    /**
     * 查询对应 repoUuid 下对应 tag 的基线， 若存在维度对应的 repo, 则获取该自定义基线数据， 否则取初始化基线数据
     * @param repoUuid 查询库
     * @param tag  查询维度
     * @return 该库的各维度基线
     */
    public RepoTagMetric getRepoMetric(String repoUuid, String tag) {
        try {
            RepoTagMetric repoTagMetric = repoMeasureMapper.getRepoTagMetric(repoUuid, tag);
            if (repoTagMetric == null) {
                // 若该库的该维度数据未更新， 则获取初始化的库维度数据
                repoTagMetric = repoMeasureMapper.getRepoTagMetric(null, tag);
            }
            log.info("get RepoMetric in {} success with tag : {}\n",repoUuid,tag);
            return repoTagMetric;
        }catch (Exception e) {
            e.getMessage();
            log.error("get RepoMetric in {} failed  with tag : {}\n",repoUuid, tag);
            return null;
        }
    }


    /**
     * 判断是否有该库该维度的记录存在
     * @param repoUuid 查询库
     * @param tag 查询维度标签
     * @return true: 已存在， false: 尚未插入
     */
    public boolean containRepoMetricOrNot(String repoUuid,String tag) {
        int num = repoMeasureMapper.containsRepoTagMetricOrNot(repoUuid,tag);
        return num >= 1;
    }

    /**
     * 更新 repo_metric 表中该维度对应库的数据基线
     * @param repoTagMetric 库维度基线数据
     * @return true : 更新成功， false : 更新失败
     */
    public boolean updateRepoMetric(RepoTagMetric repoTagMetric) {
        try {
            repoMeasureMapper.updateRepoTagMetric(repoTagMetric);
            log.info("update repoMetric success in {} with tag : {}",repoTagMetric.getRepoUuid(),repoTagMetric.getTag());
            return true;
        }catch (Exception e) {
            log.error("update repoMetric failed in {} with tag : {}",repoTagMetric.getRepoUuid(),repoTagMetric.getTag());
            return false;
        }
    }

    /**
     * 插入 repo_metric 表中该维度对应库的数据基线
     * @param repoTagMetric 库维度基线数据
     * @return true : 插入成功， false : 插入失败
     */
    public boolean insertRepoMetric(RepoTagMetric repoTagMetric) {
        try {
            repoMeasureMapper.insertRepoTagMetric(repoTagMetric);
            log.info("insert repoMetric success in {} with tag : {}",repoTagMetric.getRepoUuid(),repoTagMetric.getTag());
            return true;
        }catch (Exception e) {
            log.error("insert repoMetric failed in {} with tag : {}",repoTagMetric.getRepoUuid(),repoTagMetric.getTag());
            return false;
        }
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
