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
    public List<RepoMeasure> getRepoMeasureByRepoId(String repoId,String since,String until,Granularity granularity) {
        List<RepoMeasure> result=new ArrayList<>();
        LocalDate preTimeLimit= DateTimeUtil.parse(until).plusDays(1);
        //until查询往后推一天，例如输入的是2020-03-31，不往后推一天，接口只能返回03-30这一天及以前的数据。推一天后，就能返回03-31这一天的数据
        until = preTimeLimit.toString();
        //先统一把这个时间段的所有度量值对象都取出来，然后按照时间单位要求来过滤
        int count = 0;
        if(since == null || since.isEmpty()){
            count = 10;
        }
        List<RepoMeasure> repoMeasures=repoMeasureMapper.getRepoMeasureByDeveloperAndRepoId(repoId,null,count,since,until);
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
    public RepoMeasure getRepoMeasureByRepoIdAndCommitId(String repoId, String commitId) {
        return repoMeasureMapper.getRepoMeasureByCommit(repoId,commitId);
    }

    @Override
    public void deleteRepoMeasureByRepoId(String repoId) {
        logger.info("measurement info start to delete");
        repoMeasureMapper.delRepoMeasureByRepoId(repoId);
        repoMeasureMapper.delFileMeasureByRepoId(repoId);
        logger.info("measurement delete completed");
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

    @Override
    public List<CommitBaseInfoGranularity> getCommitBaseInfoGranularity(String repo_id, String granularity, String since, String until, String developer_name){
        //用于从数据库获取数据
        CommitBaseInfoDuration commitBaseInfoDuration;
        List<CommitBaseInfoGranularity> result=new ArrayList<>();

        //获取查询时的日期 也就是今天的日期,也作为查询时间段中的截止日期（如果until合法，就把untilDay的值赋值给today）
        LocalDate today = LocalDate.now();

        //获取最早一次提交的commit信息
        String startDateStr = repoMeasureMapper.getStartDateOfRepo(repo_id).substring(0,10);

        //最早一次commit日期
        LocalDate startDay=LocalDate.parse(startDateStr,DateTimeUtil.Y_M_D_formatter);

        //定义用于循环中不断更新的indexDay,也作为查询时间段中的开始日期（如果since合法，就把sinceDay的值赋值给indexDay）
        LocalDate indexDay = startDay;
        //以下是请求参数中的两个日期点
        if (since != null){
            LocalDate sinceDay = LocalDate.parse(dateFormatChange(since),DateTimeUtil.Y_M_D_formatter);
            if (startDay.isBefore(sinceDay)){
                indexDay = sinceDay;
            }
        }
        if (until != null){
            LocalDate untilDay = LocalDate.parse(dateFormatChange(until),DateTimeUtil.Y_M_D_formatter);
            if (today.isAfter(untilDay)){
                today = untilDay;
            }
        }
        if (indexDay.isAfter(today)){
            throw new RuntimeException("please input correct date!");
        }
        if (indexDay.equals(today)){
            commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, today.toString(), today.toString(), developer_name);
            result.add(getCommitBaseInfoGranularityData(today.toString().substring(0,10), commitBaseInfoDuration));
            return result;
        }else{
            switch (granularity){
                case "day":
                    while(today.isAfter(indexDay)){
                        //after 为 参数indexDay这天的后一天
                        LocalDate after = indexDay.plusDays(1);
//                        System.out.println(after);
                        commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, indexDay.toString(), indexDay.toString(), developer_name);
                        result.add(getCommitBaseInfoGranularityData(indexDay.toString().substring(0,10), commitBaseInfoDuration));
                        //indexDay 变成后一天
                        indexDay = after;
                    }
                    break;
                case "week":
                    while(today.isAfter(indexDay)){
                        //after 为 参数indexDay这天的后一周（后七天的那一天）
                        LocalDate after = indexDay.plusWeeks(1);
                        if(after.isAfter(today)){
                            commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, indexDay.toString(), today.toString(), developer_name);
                            result.add(getCommitBaseInfoGranularityData(indexDay.toString().substring(0,10), commitBaseInfoDuration));
                            break;
                        }
                        commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, indexDay.toString(), after.toString(), developer_name);
                        result.add(getCommitBaseInfoGranularityData(indexDay.toString().substring(0,10), commitBaseInfoDuration));
                        //indexDay 变成后七天
                        indexDay = after;
                    }
                    break;
                case "month":
                    while(today.isAfter(indexDay)){
                        //after 为 参数indexDay这天的下个月的这一天
                        LocalDate after = indexDay.plusMonths(1);
                        if(after.isAfter(today)){
                            commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, indexDay.toString(), today.toString(), developer_name);
                            result.add(getCommitBaseInfoGranularityData(indexDay.toString().substring(0,10), commitBaseInfoDuration));
                            break;
                        }
                        commitBaseInfoDuration = getCommitBaseInformationByDuration(repo_id, indexDay.toString(), after.toString(), developer_name);
                        result.add(getCommitBaseInfoGranularityData(indexDay.toString().substring(0,10), commitBaseInfoDuration));
                        //indexDay 变成indexDay这天的下个月的这一天
                        indexDay = after;
                    }
                    break;
                default:
                    throw new RuntimeException("please input correct granularity type: day or week or month");
            }
        }
        return result;
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
    public double getQuantityByCommitAndCategory(String repo_id, String commit_id, String category,String token ) {
        //代码质量指数：代码行数/问题数 若问题数为0，则返回-1
        double ratio = -1;

        //获取代码行数
        RepoMeasure repoMeasure = getRepoMeasureByRepoIdAndCommitId(repo_id,commit_id);
        if(repoMeasure != null){
            int lineCounts = repoMeasure.getNcss();
            //获取缺陷数量
            int remainIssues = restInterfaceManager.getNumberOfRemainingIssue(repo_id,commit_id,"project",null,token);

            if(remainIssues != 0){
                ratio = lineCounts*1.0/ remainIssues;
            }

        }
        return ratio;
    }

    @Override
    public Object getQuantityChangesByCommitAndCategory(String repo_id, String commit_id,String category,String token) {
        String spaceType = "project";
        category = "bug";
        Map<String,Object> changes = new HashMap<>();
        CommitBase commitBase = getCommitBaseInformation(repo_id,commit_id);
        if(commitBase != null){
            double changeLines = commitBase.getAddLines() + commitBase.getDelLines();

            int addedIssues = restInterfaceManager.getNumberOfNewIssueByCommit(repo_id,commit_id,category,spaceType,token);
//            System.out.println("addedIssues:" + addedIssues);
            if (addedIssues != -1){
                if (addedIssues != 0 ){
                    //新增问题质量指数：每改变100行代码，新增的问题数量
                    changes.put("addedQuantity", addedIssues*100*1.0/changeLines);
                }else{
                    changes.put("addedQuantity", -1);
                }
            }else{
                changes.put("未取得结果","ScanResult 未记录该commit");
                logger.error("ScanResult 未记录该commit");
            }

            int eliminatedIssues = restInterfaceManager.getNumberOfEliminateIssueByCommit(repo_id,commit_id,category,spaceType,token);
//            System.out.println("eliminatedIssues:" + eliminatedIssues);
            if(eliminatedIssues != -1){
                if(eliminatedIssues != 0){
                    //消除问题质量指数：每改变100行代码，消除的问题数量
                    changes.put("eliminatedQuantity", eliminatedIssues*100*1.0/changeLines);
                }else{
                    changes.put("eliminatedQuantity", -1);
                }
            }else{
                changes.put("未取得结果","ScanResult 未记录该commit");
                logger.error("ScanResult 未记录该commit");
            }

        }else {
            changes.put("未取得结果","未取得repo_measure记录");
            logger.error("not get repo path!");
        }



        return changes;
    }

    @Override
    public String getActivityByRepoId(String repo_id) {
        String active = null;
        LocalDate nowDate = LocalDate.now();
        int nowYear = nowDate.getYear();
        int nowMonth = nowDate.getMonthValue();
        int nowDay = nowDate.getDayOfMonth();
        LocalDate startDate = nowDate.minusDays(90);
        int startYear = startDate.getYear();
        int startMonth = startDate.getMonthValue();
        int startDay = startDate.getDayOfMonth();
        String startMonthStr = String.valueOf(startMonth);
        String startDayStr = String.valueOf(startDay);
        String nowMonthStr = String.valueOf(nowMonth);
        String nowDayStr = String.valueOf(nowDay);
        if (startMonth<10){
            startMonthStr = "0" + startMonth;
        }
        if (startDay<10){
            startDayStr = "0" + startDay;
        }
        if (nowMonth<10){
            nowMonthStr = "0" + nowMonth;
        }
        if (nowDay<10){
            nowDayStr = "0" + nowDay;
        }

        String start = startYear+"."+startMonthStr+"."+startDayStr;
        String now = nowYear+"."+nowMonthStr+"."+nowDayStr;
//        System.out.println(start);
//        System.out.println(now);
        int commitCount = getCommitCountsByDuration(repo_id,start,now);
        if(commitCount != -1){
            if(commitCount <= inactive){
                active = "inactive";
            }else if(commitCount <= lessActive){
                active = "lessActive";
            }else if(commitCount <= relativelyActive){
                active = "relativelyActive";
            }else{
                active = "active";
            }
        }
        return active;
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
    public Object getCommitCountsDaily(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);
        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            Map<String, Object> map = new HashMap<>();
            int CommitCounts = repoMeasureMapper.getCommitCountsByDuration(repo_id, indexDay.toString(), indexDay.toString(),null);
            map.put("commit_date", indexDay.toString());
            map.put("commit_count", CommitCounts);
            result.add(map);
            indexDay = indexDay.plusDays(1);
        }
        return result;
    }

    @Override
    public Object getRepoLOCByDuration(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        return repoMeasureMapper.getRepoLOCByDuration(repo_id, since, until, null);
    }

    @Override
    public Object getLOCDaily(String repo_id, String since, String until){
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);
        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            Map<String, Object> map = new HashMap<>();
            int LOC = repoMeasureMapper.getRepoLOCByDuration(repo_id, indexDay.toString(), indexDay.toString(),null);
            map.put("commit_date", indexDay.toString());
            map.put("LOC", LOC);
            result.add(map);
            indexDay = indexDay.plusDays(1);
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

    @Override
    public Object getDeveloperListByRepoId(String repo_id) {
        return repoMeasureMapper.getDeveloperListByRepoId(repo_id);
    }

}
