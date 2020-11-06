package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureRepoService;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MeasureRepoServiceImpl implements MeasureRepoService {

    private Logger logger = LoggerFactory.getLogger(MeasureRepoServiceImpl.class);

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

    public MeasureRepoServiceImpl(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Override
    public List<RepoMeasure> getRepoMeasureByrepoUuid(String repoUuid,String since,String until,Granularity granularity) {
        List<RepoMeasure> result=new ArrayList<>();
        LocalDate preTimeLimit= DateTimeUtil.parse(until).plusDays(1);
        //until查询往后推一天，例如输入的是2020-03-31，不往后推一天，接口只能返回03-30这一天及以前的数据。推一天后，就能返回03-31这一天的数据
        until = preTimeLimit.toString();
        //先统一把这个时间段的所有度量值对象都取出来，然后按照时间单位要求来过滤
        int count = 0;
        if(since == null || since.isEmpty()){
            count = 10;
        }
        List<RepoMeasure> repoMeasures=repoMeasureMapper.getRepoMeasureByDeveloperAndrepoUuid(repoUuid,null,count,since,until);
        if(repoMeasures==null||repoMeasures.isEmpty()) {
            return Collections.emptyList();
        }

        //过滤符合要求的repoMeasure，并且按照日期归类
        Map<LocalDate,List<RepoMeasure>> map=repoMeasures.stream()
        .collect(Collectors.groupingBy((RepoMeasure repoMeasure)->{
            String dateStr=repoMeasure.getCommit_time().split(" ")[0];
            return LocalDate.parse(dateStr,DateTimeUtil.Y_M_D_formatter);
        }));


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

        result= result.stream().map(rm -> {
            String date=rm.getCommit_time();
            rm.setCommit_time(date.split(" ")[0]);
            return rm;
        }).collect(Collectors.toList());


        return result;
    }

    @Override
    public RepoMeasure getRepoMeasureByrepoUuidAndCommitId(String repoUuid, String commitId) {
        return repoMeasureMapper.getRepoMeasureByCommit(repoUuid,commitId);
    }


    @Override
    public CommitBase getCommitBaseInformation(String repo_id, String commit_id) {

        return repoMeasureMapper.getCommitBaseInformation(repo_id,commit_id);
    }

    /**
     *     该方法返回一段时间内某个开发者的工作量指标，如果不指定开发者这个参数，则返回所有开发者在该项目中的工作量指标
     */
    @Override
    public CommitBaseInfoDuration getCommitBaseInformationByDuration(String repo_id, String since, String until, String developer_name) {
        CommitBaseInfoDuration commitBaseInfoDuration = new CommitBaseInfoDuration();
        String sinceDay = dateFormatChange(since);
        String untilDay = dateFormatChange(until);

        List<CommitInfoDeveloper> commitInfoDeveloper = repoMeasureMapper.getCommitInfoDeveloperListByDuration(repo_id, sinceDay, untilDay, developer_name);
        commitInfoDeveloper.removeIf(info -> info.getAuthor() == null || "".equals(info.getAuthor()));
        int addLines = repoMeasureMapper.getAddLinesByDuration(repo_id, sinceDay, untilDay, "");
        int delLines = repoMeasureMapper.getDelLinesByDuration(repo_id, sinceDay, untilDay, "");
        int sumCommitCounts = repoMeasureMapper.getCommitCountsByDuration(repo_id, sinceDay, untilDay,null);
        int sumChangedFiles = repoMeasureMapper.getChangedFilesByDuration(repo_id, sinceDay, untilDay,null);
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


    @Override
    public int getCommitCountsByDuration(String repo_id, String since, String until) {
        if (since.compareTo(until)>0 || since.length()>10 || until.length()>10){
            throw new RuntimeException("please input correct date");
        }
        String sinceday = dateFormatChange(since);
        String untilday = dateFormatChange(until);

        return repoMeasureMapper.getCommitCountsByDuration(repo_id, sinceday, untilday,null);
    }

    /**
     *     把日期格式从“2010.10.10转化为2010-10-10”
     */
    private String dateFormatChange(String dateStr){
        return dateStr.replace('.','-');
    }





    @Override
    public Object getDeveloperRankByCommitCount(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        return repoMeasureMapper.getDeveloperRankByCommitCount(repo_id, since, until);
    }

    @Override
    public Object getDeveloperRankByLoc(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        List<Map<String, Object>> result = repoMeasureMapper.getDeveloperRankByLoc(repo_id, since, until);
        //如果LOC数据为0，则删除这条数据
        if (null != result && result.size() > 0) {
            for (int i = result.size() - 1; i >= 0; i--) {
                Map<String, Object> map = result.get(i);
                Object obj;
                //取出map中第一个元素
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    obj = entry.getValue();
                    if (obj != null) {
                        //将Object类型转换为int类型
                        if (Integer.parseInt(String.valueOf(obj)) == 0) {
                            result.remove(i);
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }





    @Override
    public Object getCommitCountLOCDaily(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);
        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            Map<String, Object> map = new HashMap<>();
            int LOC = repoMeasureMapper.getRepoLOCByDuration(repo_id, indexDay.toString(), indexDay.toString(),null);
            int commitCounts = repoMeasureMapper.getCommitCountsByDuration(repo_id, indexDay.toString(), indexDay.plusDays(1).toString(),null);
            //这里只返回有commit的数据，并不是每天都返回
//            if (CommitCounts > 0){
//                map.put("commit_date", indexDay.toString());
//                map.put("LOC", LOC);
//                map.put("commit_count", CommitCounts);
//                result.add(map);
//            }
            //现在采用返回每天的数据，无论当天是否有commit
            map.put("commit_date", indexDay.toString());
            map.put("LOC", LOC);
            map.put("commit_count", commitCounts);
            result.add(map);
            indexDay = indexDay.plusDays(1);
        }
        return result;
    }



}
