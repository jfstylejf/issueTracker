package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.metric.RepoTagMetric;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.metric.TagBaseMetric;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MeasureRepoService {

    private Logger logger = LoggerFactory.getLogger(MeasureRepoService.class);

    private ProjectDao projectDao;
    private MeasureDao measureDao;

    @Value("${repoHome}")
    private String repoHome;
    @Value("${inactive}")
    private int inactive;
    @Value("${lessActive}")
    private int lessActive;
    @Value("${relativelyActive}")
    private int relativelyActive;


    private RestInterfaceManager restInterfaceManager;
    private RepoMeasureMapper repoMeasureMapper;

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public MeasureRepoService(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
    }

    public List<RepoMeasure> getRepoMeasureByRepoUuid(String repoUuid,String since,String until,Granularity granularity) {
        List<RepoMeasure> result=new ArrayList<>();
        LocalDate preTimeLimit= LocalDate.parse(until,dtf);
        //先统一把这个时间段的所有度量值对象都取出来，然后按照时间单位要求来过滤
        List<RepoMeasure> repoMeasures=repoMeasureMapper.getRepoMeasureByDeveloperAndrepoUuid(repoUuid,null,since,until);
        if(repoMeasures==null||repoMeasures.isEmpty()) {
            return Collections.emptyList();
        }

        //过滤符合要求的repoMeasure，并且按照日期归类
        Map<LocalDate,List<RepoMeasure>> map=repoMeasures.stream()
        .collect(Collectors.groupingBy((RepoMeasure repoMeasure)->{
            String dateStr=repoMeasure.getCommit_time().split(" ")[0];
            return LocalDate.parse(dateStr,DateTimeUtil.Y_M_D_formatter);
        }));

        List<LocalDate> dates = sortByCondition(map);
        selectByGranularity(result,dates,map,preTimeLimit,granularity);
        result= result.stream().map(rm -> {
            String date=rm.getCommit_time();
            rm.setCommit_time(date.split(" ")[0]);
            return rm;
        }).collect(Collectors.toList());
        return result;
    }

    public List<LocalDate> sortByCondition(Map<LocalDate,List<RepoMeasure>> map) {

        //对日期进行排序
        List<LocalDate> dates=new ArrayList<>(map.keySet());
        dates.sort(((o1, o2) -> {
            if(o1.equals(o2)) {
                return 0;
            }
            return o1.isBefore(o2)?1:-1;
        }));


        //将每个日期中的repo measure ，根据commit time进行排序为降序。
        for(Map.Entry<LocalDate,List<RepoMeasure>> entry : map.entrySet()){
            List<RepoMeasure> repoMeasuresOfDate = entry.getValue();
            repoMeasuresOfDate.sort( (o1, o2) -> {
                if(o1.getCommit_time().compareTo(o2.getCommit_time()) < 0){
                    return 1;
                }else{
                    return -1;
                }

            });
        }

        return dates;
    }

    public void selectByGranularity(List<RepoMeasure> result,List<LocalDate> dates,Map<LocalDate,List<RepoMeasure>> map,LocalDate preTimeLimit,Granularity granularity) {

        switch (granularity){
            case day:
                for(LocalDate localDate : dates){
                RepoMeasure dayOfLatest = map.get(localDate).get(0);
                result.add(dayOfLatest);
            }
            break;
            case week:
                for(LocalDate localDate : dates){
                    if(localDate.isAfter(preTimeLimit) || DateTimeUtil.isTheSameDay(localDate,preTimeLimit)){
                        continue;
                    }
                    while(!(localDate.isBefore(preTimeLimit) && localDate.isAfter(preTimeLimit.minusWeeks(1))) && !DateTimeUtil.isTheSameDay(localDate,preTimeLimit)){

                        preTimeLimit = preTimeLimit.minusWeeks(1);
                    }
                    result.add(map.get(localDate).get(0));
                    preTimeLimit = preTimeLimit.minusWeeks(1);

                }

                break;
            case month:
                for(LocalDate localDate : dates){
                    if(localDate.isAfter(preTimeLimit) || DateTimeUtil.isTheSameDay(localDate,preTimeLimit) && !DateTimeUtil.isTheSameDay(localDate,preTimeLimit)){
                        continue;
                    }
                    while(!(localDate.isBefore(preTimeLimit) && localDate.isAfter(preTimeLimit.minusMonths(1))) && !DateTimeUtil.isTheSameDay(localDate,preTimeLimit) ){

                        preTimeLimit = preTimeLimit.minusMonths(1);
                    }
                    result.add(map.get(localDate).get(0));
                    preTimeLimit = preTimeLimit.minusMonths(1);

                }

                break;
            default:
                throw new RuntimeException("please input correct granularity !");
        }

    }


    public RepoMeasure getRepoMeasureByrepoUuidAndCommitId(String repoUuid, String commitId) {
        return repoMeasureMapper.getRepoMeasureByCommit(repoUuid,commitId);
    }


    public CommitBase getCommitBaseInformation(String repo_id, String commit_id) {

        return repoMeasureMapper.getCommitBaseInformation(repo_id,commit_id);
    }

    /**
     *     该方法返回一段时间内某个开发者的工作量指标，如果不指定开发者这个参数，则返回所有开发者在该项目中的工作量指标
     */
    public CommitBaseInfoDuration getCommitBaseInformationByDuration(String repo_id, String since, String until, String developer_name) {
        CommitBaseInfoDuration commitBaseInfoDuration = new CommitBaseInfoDuration();
        List<CommitInfoDeveloper> commitInfoDeveloper = repoMeasureMapper.getCommitInfoDeveloperListByDuration(repo_id, since, until, developer_name);
        commitInfoDeveloper.removeIf(info -> info.getAuthor() == null || "".equals(info.getAuthor()));
        int addLines = repoMeasureMapper.getAddLinesByDuration(repo_id, since, until, "");
        int delLines = repoMeasureMapper.getDelLinesByDuration(repo_id, since, until, "");
        int sumCommitCounts = repoMeasureMapper.getCommitCountsByDuration(repo_id, since, until,null);
        int sumChangedFiles = repoMeasureMapper.getChangedFilesByDuration(repo_id, since, until,null);
        commitBaseInfoDuration.setCommitInfoList(commitInfoDeveloper);
        commitBaseInfoDuration.setSumAddLines(addLines);
        commitBaseInfoDuration.setSumDelLines(delLines);
        commitBaseInfoDuration.setSumCommitCounts(sumCommitCounts);
        commitBaseInfoDuration.setSumChangedFiles(sumChangedFiles);
        return commitBaseInfoDuration;
    }



    private CommitBaseInfoGranularity getCommitBaseInfoGranularityData(String time, CommitBaseInfoDuration commitBaseInfoDuration){
        CommitBaseInfoGranularity commitBaseInfoGranularity = new CommitBaseInfoGranularity();
        commitBaseInfoGranularity.setDate(time);
        commitBaseInfoGranularity.setCommitBaseInfoDuration(commitBaseInfoDuration);
        return commitBaseInfoGranularity;
    }


    public int getCommitCountsByDuration(String repo_id, String since, String until) {
        if (since.compareTo(until)>0 || since.length()>10 || until.length()>10){
            throw new RuntimeException("please input correct date");
        }
        return repoMeasureMapper.getCommitCountsByDuration(repo_id, since, until,null);
    }


    /**
     * 某段时间内，该项目中提交次数最多的前三名开发者的姓名以及对应的commit次数
     * @param query 查询条件
     * @return key : developerName , countNum
     */
    public List<Map<String,Object>> getDeveloperRankByCommitCount(Query query){
        return projectDao.getDeveloperRankByCommitCount(query);
    }

    /**
     * 获取所查询库列表中前3名增加代码物理行数的开发者
     * @param query 查询条件
     * @return key : developerName , developerLoc
     */
    public List<Map<String, Object>> getDeveloperRankByLoc(Query query){
        return measureDao.getDeveloperRankByLoc(query);
    }


    /**
     * 获取某段时间内，每天的所有提交次数和物理行数
     * @param query 查询条件
     * @return List<Map<String, Object>> key : commit_date, LOC,commit_count
     */
    public List<Map<String, Object>> getDailyCommitCountAndLOC(Query query){
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate untilDay = LocalDate.parse(query.getUntil(),dtf);
        LocalDate sinceDay;
        int timeDiff ;
        if(query.getSince()!=null && !"".equals(query.getSince())) {
            sinceDay = LocalDate.parse(query.getSince(),dtf);
            timeDiff = (int) (untilDay.toEpochDay() - sinceDay.toEpochDay());
        }else {
            // 默认查询一周
            sinceDay = untilDay.minusDays(7);
            timeDiff = 7;
        }
        LocalDate tempSince = sinceDay;
        for (int i=0 ;i<timeDiff; i++) {
            query.setSince(dtf.format(tempSince));
            query.setUntil(dtf.format(tempSince.plusDays(1)));
            Map<String, Object> map = new HashMap<>(6);
            int loc = 0;
            int commitCounts = 0;
            DeveloperWorkLoad developerWorkLoad = measureDao.getDeveloperWorkLoadData(query);
            if(developerWorkLoad != null) {
                loc = developerWorkLoad.getAddLines();
                commitCounts = developerWorkLoad.getCommitCount();
            }
            //现在采用返回每天的数据，无论当天是否有commit
            map.put("commit_date", dtf.format(tempSince));
            map.put("LOC", loc);
            map.put("commit_count", commitCounts);
            result.add(map);
            tempSince = tempSince.plusDays(1);
        }
        return result;
    }

    /**
     * 删除所属 repo 下 Measure 服务数据
     * @param repoUuid 删除库
     */
    @Async("taskExecutor")
    public void deleteRepoMsg(String repoUuid,String token) {
       boolean res = measureDao.deleteRepoMsg(repoUuid);
        if(res){
            boolean recallRes = restInterfaceManager.deleteRecall(repoUuid,token);
            if(recallRes){
                log.info(" recall success\n");
            }else {
                log.info(" recall false\n");
            }
        }
    }

    /**
     * 获取该库的维度基线值
     * @param repoUuid 查询库
     * @return List {@link RepoTagMetric}
     */
    public List<RepoTagMetric> getRepoMetricList(String repoUuid) {
        return measureDao.getRepoMetric(repoUuid);
    }

    /**
     * 插入或更新 库下各维度初始数据
     * @param repoTagMetric 待插入或更新的库下各维度基础数据
     */
    public void insertRepoTagMetric(RepoTagMetric repoTagMetric) throws Exception {
        String repoUuid = repoTagMetric.getRepoUuid();
        String tag = repoTagMetric.getTag();
        TagBaseMetric tagBaseMetric = new TagBaseMetric();
        Map<String,TagBaseMetric> tagBaseMetricMap = tagBaseMetric.getTagBaseMetricMap();
        if (!tagBaseMetricMap.containsKey(tag)) {
            log.error("check the tag name : {}, may be you get the wrong tag name",tag);
            throw new Exception();
        }else {
            TagBaseMetric target = tagBaseMetricMap.get(tag);
            repoTagMetric.setBestMax(target.getBestMax());
            repoTagMetric.setWorstMin(target.getWorstMin());
            // 插入时 tag 是英文
            repoTagMetric.setTag(target.getTagMetricEnum().name());
            if (measureDao.containRepoMetricOrNot(repoUuid,target.getTagMetricEnum().name())) {
                // 若已经存在记录，则更新
                measureDao.updateRepoMetric(repoTagMetric);
            }else {
                // 否则插入
                measureDao.insertRepoMetric(repoTagMetric);
            }
        }
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {this.projectDao = projectDao;}

    @Autowired
    public void setMeasureDao(MeasureDao measureDao) {this.measureDao = measureDao;};

}
