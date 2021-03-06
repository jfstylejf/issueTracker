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
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????
        List<RepoMeasure> repoMeasures=repoMeasureMapper.getRepoMeasureByDeveloperAndrepoUuid(repoUuid,null,since,until);
        if(repoMeasures==null||repoMeasures.isEmpty()) {
            return Collections.emptyList();
        }

        //?????????????????????repoMeasure???????????????????????????
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

        //?????????????????????
        List<LocalDate> dates=new ArrayList<>(map.keySet());
        dates.sort(((o1, o2) -> {
            if(o1.equals(o2)) {
                return 0;
            }
            return o1.isBefore(o2)?1:-1;
        }));


        //?????????????????????repo measure ?????????commit time????????????????????????
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
     *     ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????????????????????????????????????????commit??????
     * @param query ????????????
     * @return key : developerName , countNum
     */
    public List<Map<String,Object>> getDeveloperRankByCommitCount(Query query){
        return projectDao.getDeveloperRankByCommitCount(query);
    }

    /**
     * ??????????????????????????????3???????????????????????????????????????
     * @param query ????????????
     * @return key : developerName , developerLoc
     */
    public List<Map<String, Object>> getDeveloperRankByLoc(Query query){
        return measureDao.getDeveloperRankByLoc(query);
    }


    /**
     * ??????????????????????????????????????????????????????????????????
     * @param query ????????????
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
            // ??????????????????
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
            //?????????????????????????????????????????????????????????commit
            map.put("commit_date", dtf.format(tempSince));
            map.put("LOC", loc);
            map.put("commit_count", commitCounts);
            result.add(map);
            tempSince = tempSince.plusDays(1);
        }
        return result;
    }

    /**
     * ???????????? repo ??? Measure ????????????
     * @param repoUuid ?????????
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
     * ??????????????????????????????
     * @param repoUuid ?????????
     * @return List {@link RepoTagMetric}
     */
    public List<RepoTagMetric> getRepoMetricList(String repoUuid) {
        return measureDao.getRepoMetric(repoUuid);
    }

    /**
     * ??????????????? ???????????????????????????
     * @param repoTagMetric ????????????????????????????????????????????????
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
            // ????????? tag ?????????
            repoTagMetric.setTag(target.getTagMetricEnum().name());
            if (measureDao.containRepoMetricOrNot(repoUuid,target.getTagMetricEnum().name())) {
                // ?????????????????????????????????
                measureDao.updateRepoMetric(repoTagMetric);
            }else {
                // ????????????
                measureDao.insertRepoMetric(repoTagMetric);
            }
        }
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {this.projectDao = projectDao;}

    @Autowired
    public void setMeasureDao(MeasureDao measureDao) {this.measureDao = measureDao;};

}
