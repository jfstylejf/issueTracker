package cn.edu.fudan.cloneservice.service.impl;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.*;
import cn.edu.fudan.cloneservice.domain.*;
import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.mapper.CloneMeasureMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import cn.edu.fudan.cloneservice.mapper.RepoMetricMapper;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.thread.ForkJoinRecursiveTask;
import cn.edu.fudan.cloneservice.util.ComputeUtil;
import cn.edu.fudan.cloneservice.util.DateTimeUtil;
import cn.edu.fudan.cloneservice.util.JGitUtil;
import cn.edu.fudan.cloneservice.util.UserUtil;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 */
@Slf4j
@Service
@Component
public class CloneMeasureServiceImpl implements CloneMeasureService {

    private final RepoCommitMapper repoCommitMapper;
    private final RestInterfaceManager restInterfaceManager;
    private final CloneMeasureDao cloneMeasureDao;
    private final CloneInfoDao cloneInfoDao;
    protected CloneLocationDao cloneLocationDao;
    private final ForkJoinRecursiveTask forkJoinRecursiveTask;
    private final CloneMeasureMapper cloneMeasureMapper;
    private final UserUtil userUtil;
    private final RepoCommitDao repoCommitDao;
    private final RepoMeasureDao repoMeasureDao;
    private final RepoMetricMapper repoMetricMapper;

    @Autowired
    public CloneMeasureServiceImpl(RepoCommitMapper repoCommitMapper, RestInterfaceManager restInterfaceManager, CloneMeasureDao cloneMeasureDao, CloneInfoDao cloneInfoDao, CloneLocationDao cloneLocationDao, ForkJoinRecursiveTask forkJoinRecursiveTask, CloneMeasureMapper cloneMeasureMapper, UserUtil userUtil, RepoCommitDao repoCommitDao, RepoMeasureDao repoMeasureDao, RepoMetricMapper repoMetricMapper) {
        this.repoCommitMapper = repoCommitMapper;
        this.restInterfaceManager = restInterfaceManager;
        this.cloneMeasureDao = cloneMeasureDao;
        this.cloneInfoDao = cloneInfoDao;
        this.cloneLocationDao = cloneLocationDao;
        this.forkJoinRecursiveTask = forkJoinRecursiveTask;
        this.cloneMeasureMapper = cloneMeasureMapper;
        this.repoMeasureDao = repoMeasureDao;
        this.userUtil = userUtil;
        this.repoCommitDao = repoCommitDao;
        this.repoMetricMapper = repoMetricMapper;
    }
    @Override
    public List<CloneMessage> getCloneMeasure(String repositoryId, String developers, String start, String end) {
        List<CloneMessage> cloneMessages = new ArrayList<>();
        if (StringUtils.isEmpty(developers)) {
            if (StringUtils.isEmpty(repositoryId)) {
                log.error("repositoryId and developer is null");
                return cloneMessages;
            }
            List<String> repoIds = split(repositoryId);
            // ??????repo ?????? ?????????????????????git accountName
            List<String> developerList = getDeveloperList(start, end, "", repoIds);
            // ?????? key gitName value trueName
            Map<String, String> trueNameGitName = getName(developerList);
            if (trueNameGitName.isEmpty()) return cloneMessages;
            List<String> developerListSimply = new ArrayList<>();
            Iterator<Map.Entry<String, String>> it = trueNameGitName.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                if (entry.getValue() != null) {
                    developerListSimply.add(entry.getKey());
                }
            }

            List<CloneMessage> gitNameClone = new ArrayList<>(developerListSimply.size());
            log.info("loop:" + developerListSimply.size());
            developerListSimply.forEach(d -> gitNameClone.add(getOneDeveloperCloneInfo(repositoryId, d, start, end, trueNameGitName.get(d))));

            Map<String, List<CloneMessage>> map = gitNameClone.parallelStream().collect(Collectors.groupingBy(CloneMessage::getDeveloperName));
            map.values().forEach(c -> cloneMessages.add(union(c)));
        } else {
            List<String> developerList = split(developers);
            for (String developer : developerList) {
                List<CloneMessage> cloneMessageEach = new ArrayList<>();
                List<String> gitName = repoCommitMapper.getAllGitName(developer);
                gitName.forEach(n -> cloneMessageEach.add(getOneDeveloperCloneInfo(repositoryId, n, start, end, developer)));
                CloneMessage u = union(cloneMessageEach);
                cloneMessages.add(u);
            }
        }
        return cloneMessages;
    }

    private List<String> getDeveloperList(String start, String end, String developers, List<String> repoIds) {
        List<String> developerList = new ArrayList<>();
        if (!StringUtil.isEmpty(developers)) {
            return split(developers);
        } else {
            repoIds.forEach(repoId -> developerList.addAll(cloneMeasureMapper.getAllDeveloper(repoId, start, end)));
            return developerList;
        }
    }

    /**
     * fixme ????????????????????? ?????????????????????????????????????????????
     */
    private CloneMessage union(List<CloneMessage> cloneMessageList) {
        if (cloneMessageList == null || cloneMessageList.isEmpty()) {
            return new CloneMessage();
        }
        CloneMessage result = cloneMessageList.get(0);
        for (int i = 1; i < cloneMessageList.size(); i++) {

            CloneMessage c = cloneMessageList.get(i);
            result.setIncreasedCloneLines(result.getIncreasedCloneLines() + c.getIncreasedCloneLines());
            result.setSelfIncreasedCloneLines(result.getSelfIncreasedCloneLines() + c.getSelfIncreasedCloneLines());
            result.setOthersIncreasedCloneLines(result.getOthersIncreasedCloneLines() + c.getOthersIncreasedCloneLines());
            result.setEliminateCloneLines(result.getEliminateCloneLines() + c.getEliminateCloneLines());
            if ((result.getRepoUuid() == null && c.getRepoUuid() == null) || (!result.getRepoUuid().equals(c.getRepoUuid()))) {
                result.setAllEliminateCloneLines(result.getAllEliminateCloneLines() + c.getAllEliminateCloneLines());
            }
        }
        result.setIncreasedCloneLinesRate(result.getIncreasedCloneLines() + "/" + result.getAddLines());
        return result;
    }

    private Map<String, String> getName(List<String> developerList) {
        Map<String, String> result = new HashMap<>(developerList.size() >> 1);
        List<Map<String, String>> trueName = repoCommitMapper.getAllTrueName();
        for (Map<String, String> m : trueName) {
            String name = m.get("account_name");
            String gitName = m.get("account_gitname");
            if (developerList.contains(gitName)) {
                result.put(gitName, name);
            }
        }
        return result;
    }


    private CloneMessage getOneDeveloperCloneInfo(String repositoryId, String developer, String start, String end, String trueName) {
        List<String> repoIds = new ArrayList<>();
        String trim = ",";
        String[] targetRepos = new String[0];
        if (repositoryId.contains(trim)) {
            targetRepos = repositoryId.split(trim);
            repositoryId = null;
        }
        repoIds.add(repositoryId);
        if (StringUtils.isEmpty(repositoryId)) {
            repoIds = repoCommitMapper.getrepoIdList(developer);
        }
        if (targetRepos.length != 0) {
            repoIds = Arrays.asList(targetRepos);
        }

        int newCloneLines = 0;
        int selfCloneLines = 0;
        int deleteCloneLines = 0;
        int allDeleteCloneLines = 0;
        int addLines = 0;
        for (String repoId : repoIds) {
            List<String> developerCommitList = repoCommitMapper.getAuthorCommitList(repoId, developer, start, end);
            List<String> repoCommitList = repoCommitMapper.getCommitList(repoId, start, end);
            List<CloneMeasure> cloneMeasures = cloneMeasureDao.getCloneMeasures(repoId);


            int preCloneLines = 0;
            for (String commitId : repoCommitList) {
                for (CloneMeasure cloneMeasure1 : cloneMeasures) {
                    //????????????
                    if (cloneMeasure1.getCommitId().equals(commitId) && developerCommitList.contains(commitId)) {
                        newCloneLines += cloneMeasure1.getNewCloneLines();
                        selfCloneLines += cloneMeasure1.getSelfCloneLines();
                        if (preCloneLines > cloneMeasure1.getCloneLines()) {
                            deleteCloneLines += preCloneLines - cloneMeasure1.getCloneLines();
                        }
                    }
                    //????????????
                    if (cloneMeasure1.getCommitId().equals(commitId)) {
                        //??????clone??????,???????????????
                        if (preCloneLines > cloneMeasure1.getCloneLines()) {
                            allDeleteCloneLines += preCloneLines - cloneMeasure1.getCloneLines();
                        }
                        preCloneLines = cloneMeasure1.getCloneLines();
                        break;
                    }
                }
            }
            addLines += restInterfaceManager.getAddLines(repoId, start, end, trueName);
        }

        CloneMessage cloneMessage = new CloneMessage();
        cloneMessage.setDeveloperName(trueName);
        if (StringUtils.isEmpty(trueName)) {
            cloneMessage.setDeveloperName(developer);
        }
        cloneMessage.setRepoUuid(repositoryId);
        cloneMessage.setIncreasedCloneLines(newCloneLines);
        cloneMessage.setSelfIncreasedCloneLines(selfCloneLines);
        cloneMessage.setAddLines(addLines);
        cloneMessage.setEliminateCloneLines(deleteCloneLines);
        cloneMessage.setAllEliminateCloneLines(allDeleteCloneLines);
        cloneMessage.setOthersIncreasedCloneLines(newCloneLines - selfCloneLines);
        return cloneMessage;
    }

    /**
     * ?????????????????????clone??????
     * ???????????????????????????
     *
     * @param repoId   repo id
     * @param commitId commit id
     * @return ??????
     */
    private int getCloneLines(String repoId, String commitId) {

        int cloneLinesWithOutTest = 0;
        if (commitId != null) {
            List<CloneLocation> cloneLocations = cloneLocationDao.getCloneLocations(repoId, commitId);
            Map<String, List<String>> locationMap = new HashMap<>(512);
            for (CloneLocation location : cloneLocations) {
                String[] nums = location.getNum().split(",");
                for (String line : nums) {
                    ComputeUtil.putNewNum(locationMap, line, location.getFilePath());
                }
            }
            log.info("repoId:{} - commitId:{}--->cloneLines:{}", repoId, commitId, cloneLinesWithOutTest);
            return ComputeUtil.getCloneLines(locationMap);
        }

        return -1;
    }

    @Override
    public void insertCloneMeasure(String repoId, String commitId, String repoPath) {
        //???????????????????????????????????????????????????
        if (cloneMeasureDao.getCloneMeasureCount(repoId, commitId) > 0) {
            return;
        }
        int increasedLines;
        int currentCloneLines;
        Map<String, String> map;
        //??????java???js
        CommitChange commitChange = JGitUtil.getNewlyIncreasedLines(repoPath, commitId);
        JGitUtil jGitHelper = new JGitUtil(repoPath);
        Date commitTime = new Date(jGitHelper.getLongCommitTime(commitId));
        currentCloneLines = getCloneLines(repoId, commitId);
        map = commitChange.getAddMap();
        increasedLines = commitChange.getAddLines();
        List<CloneLocation> cloneLocations = cloneLocationDao.getCloneLocations(repoId, commitId);
        log.info("location list size : {}", cloneLocations.size());
        Map<String, List<CloneLocation>> cloneLocationMap = new HashMap<>(512);
        log.info("clone measure {} -> cloneLocation init start!", Thread.currentThread().getName());
        //?????????
        for (CloneLocation cloneLocation : cloneLocations) {
            String category = cloneLocation.getCategory();
            if (cloneLocationMap.containsKey(category)) {
                cloneLocationMap.get(category).add(cloneLocation);
            } else {
                List<CloneLocation> locations = new ArrayList<>();
                locations.add(cloneLocation);
                cloneLocationMap.put(category, locations);
            }
        }
        //key??????repoPath, value??????????????????clone?????????
        Map<String, String> addCloneLocationMap;
        //key??????repoPath, value??????????????????self clone?????????
        Map<String, String> selfCloneLocationMap;


        //cloneLocations:all clone locations by commit; map:add lines map; cloneLocationMap:divided by category
        CloneMeasure cloneMeasure = forkJoinRecursiveTask.extract(repoId, commitId, repoPath, cloneLocations, cloneLocationMap, map);
        if (cloneMeasure == null) {
            return;
        }

        addCloneLocationMap = cloneMeasure.getAddCloneLocationMap();
        selfCloneLocationMap = cloneMeasure.getSelfCloneLocationMap();
        //??????cloneInfo
        List<CloneInfo> cloneInfoList = getCloneInfoList(repoId, commitId, addCloneLocationMap, selfCloneLocationMap);

        cloneMeasure.setAddLines(increasedLines);
        cloneMeasure.setCloneLines(currentCloneLines);
        cloneMeasure.setCommitTime(commitTime);
        cloneMeasureDao.insertCloneMeasure(cloneMeasure);
        log.info("{} -> cloneInfoList size : {}", Thread.currentThread().getName(), cloneInfoList.size());
        if (cloneInfoList.size() > 0) {
            cloneInfoDao.insertCloneInfo(cloneInfoList);
        }
    }

    private List<CloneInfo> getCloneInfoList(String repoId, String commitId, Map<String, String> addCloneLocationMap, Map<String, String> selfCloneLocationMap) {
        List<CloneInfo> cloneInfoList = new ArrayList<>();
        for (String clone : addCloneLocationMap.keySet()) {
            String type = clone.substring(0, clone.indexOf(":"));
            String filePath = clone.substring(clone.indexOf(":") + 1);
            String uuid = UUID.randomUUID().toString();
            String selfCloneLines = null;
            if (selfCloneLocationMap.containsKey(clone)) {
                selfCloneLines = selfCloneLocationMap.get(clone);
            }
            CloneInfo cloneInfo = new CloneInfo(uuid, repoId, commitId, filePath, addCloneLocationMap.get(clone), selfCloneLines, type);
            cloneInfoList.add(cloneInfo);
        }

        return cloneInfoList;
    }


    @Override
    public CloneMeasure getLatestCloneMeasure(String repositoryId) {
        List<String> repoIds = new ArrayList<>();
        String trim = ",";
        String[] targetRepos = new String[0];
        if (repositoryId.contains(trim)) {
            targetRepos = repositoryId.split(trim);
            repositoryId = null;
        }
        repoIds.add(repositoryId);

        if (targetRepos.length != 0) {
            repoIds = Arrays.asList(targetRepos);
        }
        return cloneMeasureDao.getLatestCloneLines(repoIds);
    }

    List<String> split(String repositoryId) {
        List<String> repoIds = new ArrayList<>();
        String trim = ",";
        String[] targetRepos = new String[0];

        if (repositoryId.contains(trim)) {
            targetRepos = repositoryId.split(trim);
            repositoryId = null;
        }
        repoIds.add(repositoryId);

        if (targetRepos.length != 0) {
            repoIds = Arrays.asList(targetRepos);
        }
        return repoIds;
    }

    @Override
    public List<CloneMessage> sortByOrder(List<CloneMessage> cloneMessages, String order) {
        if (cloneMessages.isEmpty()) return new ArrayList<>();
        switch (order) {
            case "increasedCloneLinesRate":
                Collections.sort(cloneMessages);
                break;
            case "increasedCloneLines":
                Comparator<CloneMessage> byIncreasedCloneLines = Comparator.comparing(CloneMessage::getIncreasedCloneLines);
                cloneMessages.sort(byIncreasedCloneLines);
                break;
            case "selfIncreasedCloneLines":
                Comparator<CloneMessage> bySelfIncreasedCloneLines = Comparator.comparing(CloneMessage::getSelfIncreasedCloneLines);
                cloneMessages.sort(bySelfIncreasedCloneLines);
                break;
            case "othersIncreasedCloneLines":
                Comparator<CloneMessage> byOthersIncreasedCloneLines = Comparator.comparing(CloneMessage::getOthersIncreasedCloneLines);
                cloneMessages.sort(byOthersIncreasedCloneLines);
                break;
            case "eliminateCloneLines":
                Comparator<CloneMessage> byEliminateCloneLines = Comparator.comparing(CloneMessage::getEliminateCloneLines);
                cloneMessages.sort(byEliminateCloneLines);
                break;
            case "allEliminateCloneLines":
                Comparator<CloneMessage> byAllEliminateCloneLines = Comparator.comparing(CloneMessage::getAllEliminateCloneLines);
                cloneMessages.sort(byAllEliminateCloneLines);
                break;
            case "addLines":
                Comparator<CloneMessage> byAddLines = Comparator.comparing(CloneMessage::getAddLines);
                cloneMessages.sort(byAddLines);
                break;
            case "repoId":
                Comparator<CloneMessage> byRepoId = Comparator.comparing(CloneMessage::getRepoUuid);
                cloneMessages.sort(byRepoId);
                break;
            case "developer":
                Comparator<CloneMessage> byDeveloper = Comparator.comparing(CloneMessage::getDeveloperName);
                cloneMessages.sort(byDeveloper);
                break;
            default:
                Collections.sort(cloneMessages);
        }
        return cloneMessages;
    }

    @Cacheable(value = "trend_graph", key = "#projectId + '_' + #until + '_' + #since + '_' + #interval")
    public List<CloneGroupSum> getCloneGroupsSum(List<String> projectList, String since, String until, String interval, String projectId) {
        List<LocalDate> timeList = getTimeList(since, until, interval);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<CloneGroupSum> results = new ArrayList<>();
        log.info("get project success");
        for (String aProjectId : projectList) {
            String projectName = repoCommitDao.getProjectNameByProjectId(aProjectId);
            timeList.forEach(a -> results.add(new CloneGroupSum(projectName, aProjectId, a.format(dtf), cloneLocationDao.getCloneLocationGroupSum(repoCommitMapper.getRepoIdByProjectId(aProjectId), a.format(dtf)))));
        }
        return results;
    }

    @Override
    public List<CloneGroupSum> getTrendGraph(String projectId, String since, String until, String interval, String token) {
        List<String> projectList = getProjectIds(projectId, token);
        return ((CloneMeasureServiceImpl) AopContext.currentProxy()).getCloneGroupsSum(projectList, since, until, interval, projectId);
    }

    @Scheduled(cron = "0 0 0 ? * SUN")
    public void getCloneGroupSumWeekly() {
        String token = "ec15d79e36e14dd258cfff3d48b73d35";
        getTrendGraph(null, null, null, "week", token);
    }

    @Override
    public List<CloneOverallView> getCloneOverallViews(String projectId, String repoUuid, String until, String token) {
        List<String> projectList = getProjectIds(projectId, token);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(until, dtf);
        List<CloneOverallView> results = new ArrayList<>();
        for (String aProjectId : projectList) {
            String projectName = repoCommitDao.getProjectNameByProjectId(aProjectId);
            List<String> projectIdList = new ArrayList<>();
            projectIdList.add(aProjectId);
            results.addAll(cloneLocationDao.getCloneOverall(getRepoUuids(projectIdList, repoUuid), localDate.format(dtf), aProjectId, projectName));
        }
        return results;
    }

    private List<String> getProjectIds(String projectId, String token) {
        List<String> projectIds = new ArrayList<>();
        if (StringUtils.isEmpty(projectId)) {
            List<Integer> temp = repoCommitMapper.getProjectIds();
            temp.forEach(a -> projectIds.add(a.toString()));
        } else {
            projectIds.addAll(Arrays.asList(projectId.split(",")));
        }
        List<Integer> projectsWithRightTemp = userUtil.getVisibleProjectByToken(token);
        List<String> projectsWithRight = new ArrayList<>();
        if (!StringUtils.isEmpty(projectsWithRightTemp)) {
            projectsWithRightTemp.forEach(a -> projectsWithRight.add(a.toString()));
        }
        return projectIds.stream().filter(projectsWithRight::contains).collect(Collectors.toList());
    }

    private List<String> getRepoUuids(List<String> projectIds, String repoUuid) {
        List<String> repoUuids = new ArrayList<>();
        if (projectIds.isEmpty()) {
            List<Integer> temp = repoCommitMapper.getProjectIds();
            temp.forEach(a -> projectIds.add(a.toString()));
        }
        for (String projectId : projectIds) {
            repoUuids.addAll(repoCommitMapper.getRepoIdByProjectId(projectId));
        }
        if (!StringUtils.isEmpty(repoUuid)) {
            repoUuids.retainAll(Arrays.asList(repoUuid.split(",")));
        }
        return repoUuids;
    }

    @Override
    public List<CloneDetail> getCloneDetails(String projectId, String groupId, String commitId, String token) {
        List<String> projectList = getProjectIds(projectId, token);
        List<CloneDetail> results = new ArrayList<>();
        for (String aProjectId : projectList) {
            String projectName = repoCommitDao.getProjectNameByProjectId(aProjectId);
            String repoId = repoCommitMapper.getRepoIdByCommitId(commitId);
            cloneLocationDao.getCloneDetail(repoId, projectId, groupId, projectName, commitId);
            results.addAll(cloneLocationDao.getCloneDetail(repoId, projectId, groupId, projectName, commitId));
        }
        return results;
    }

    @Override
    public List<CloneDetailOverall> getCloneDetailOverall(String projectId, String commitId, String repoUuid, String until, String token) {
        List<String> projectList = getProjectIds(projectId, token);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(until, dtf);
        List<CloneDetailOverall> results = new ArrayList<>();
        for (String aProjectId : projectList) {
            String projectName = repoCommitDao.getProjectNameByProjectId(aProjectId);
            results.addAll(cloneLocationDao.getCloneDetailOverall(aProjectId, projectName, getRepoUuids(projectList, repoUuid), commitId, localDate.format(dtf)));
        }
        return results;
    }

    private List<LocalDate> getTimeList(String since, String until, String interval) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endTime = LocalDate.now();
        if (!StringUtil.isEmpty(until)) {
            endTime = LocalDate.parse(until, dtf);
        }
        LocalDate beginTime;
        // fixme since?????????????????????
        if (since != null && !"".equals(since)) {
            beginTime = LocalDate.parse(since, dtf);
        } else {
            beginTime = endTime.minusWeeks(6);
        }
        beginTime = DateTimeUtil.initEndTimeByInterval(beginTime, interval);
        endTime = DateTimeUtil.initEndTimeByInterval(endTime, interval);
        List<LocalDate> time = new ArrayList<>();

        while (true) {
            assert beginTime != null;
            assert endTime != null;
            if (!(beginTime.isBefore(endTime) || beginTime.isEqual(endTime))) break;
            time.add(beginTime);
            LocalDate tempTime = DateTimeUtil.selectTimeIncrementByInterval(beginTime, interval);
            if (tempTime == null) {
                break;
            }
            if (tempTime.isAfter(endTime)) {
                break;
            }
            beginTime = tempTime;
        }
        return time;
    }

    @Override
    public List<CloneMessage> getCloneLine(String projectId, String repoUuid, String developers, String since, String until, String token) {
        List<String> repoIds = getRepoUuids(getProjectIds(projectId, token), repoUuid);
        List<CloneMessage> result = new ArrayList<>();
        List<String> developerList = getDeveloperListByParam(developers);
        for (String developer : developerList) {
            int newCloneLines = 0;
            int deleteCloneLines = 0;
            int addLines = repoMeasureDao.getRepoAddLines(repoIds, developer, since, until);
            for (String repoId : repoIds) {
                List<CloneMeasure> cloneMeasures = cloneMeasureDao.getCloneMeasureByDeveloperAndDuration(repoId, developer, since, until);

                int preCloneLines = 0;
                for (CloneMeasure cloneMeasure1 : cloneMeasures) {
                    //????????????
                    newCloneLines += cloneMeasure1.getNewCloneLines();
                    if (preCloneLines > cloneMeasure1.getCloneLines()) {
                        deleteCloneLines += preCloneLines - cloneMeasure1.getCloneLines();
                    }
                }
            }

            CloneMessage cloneMessage = new CloneMessage();

            if (!StringUtils.isEmpty(developer)) {
                cloneMessage.setDeveloperName(developer);
            }
            RepoTagMetric repoTagMetric = repoMetricMapper.getRepoCloneLineMetric(null);
            cloneMessage.setRepoUuid(repoUuid);
            cloneMessage.setIncreasedCloneLines(newCloneLines);
            cloneMessage.setAddLines(addLines);
            cloneMessage.setEliminateCloneLines(deleteCloneLines);
            if(addLines!=0) {
                cloneMessage.setLevel(repoTagMetric.getLevel((double) newCloneLines / (double) addLines));
            }else {
                cloneMessage.setLevel("none");
            }
            result.add(cloneMessage);
        }

        return result;
    }

    private List<String> getDeveloperListByParam(String developers) {
        List<String> developerList;
        if (!StringUtil.isEmpty(developers)) {
            developerList = split(developers);
        } else {
            developerList = repoCommitMapper.getDevelopers();
        }
        return developerList;
    }
}

