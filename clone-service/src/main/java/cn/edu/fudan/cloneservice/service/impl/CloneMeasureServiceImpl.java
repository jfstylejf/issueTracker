package cn.edu.fudan.cloneservice.service.impl;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.*;
import cn.edu.fudan.cloneservice.domain.CloneInfo;
import cn.edu.fudan.cloneservice.domain.CloneMeasure;
import cn.edu.fudan.cloneservice.domain.CloneMessage;
import cn.edu.fudan.cloneservice.domain.CommitChange;
import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.mapper.CloneInfoMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.service.ScanService;
import cn.edu.fudan.cloneservice.thread.ForkJoinRecursiveTask;
import cn.edu.fudan.cloneservice.util.ComputeUtil;
import cn.edu.fudan.cloneservice.util.JGitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 */
@Slf4j
@Service
public class CloneMeasureServiceImpl implements CloneMeasureService {

    private RepoCommitMapper repoCommitMapper;
    private RestInterfaceManager restInterfaceManager;
    private CloneMeasureDao cloneMeasureDao;
    private CloneInfoDao cloneInfoDao;
    private CloneLocationDao cloneLocationDao;
    private ForkJoinRecursiveTask forkJoinRecursiveTask;
    private CloneInfoMapper cloneInfoMapper;

    @Override
    public List<CloneMessage> getCloneMeasure(String repositoryIds, String developer, String start, String end) {

        List<CloneMessage> cloneMessages = new ArrayList<>();

        List<String> repoIds = new ArrayList<>();
        String trim = ",";
        String [] targetRepos  = new String[0];
        if (repositoryIds.contains(trim)) {
            targetRepos = repositoryIds.split(trim);
            repositoryIds = null;
        }
        repoIds.add(repositoryIds);
        if (targetRepos.length != 0) {
            repoIds = Arrays.asList(targetRepos);
        }

        for(String repositoryId: repoIds) {
            if (StringUtils.isEmpty(developer)) {
                if (StringUtils.isEmpty(repositoryId)) {
                    log.error("repositoryId and developer is null");
                    return cloneMessages;
                }
                // 单个repo 维度 存放的是用户的git accountName
                List<String> developerList = repoCommitMapper.getAllDeveloper(repositoryId);
                // 聚合 key gitName value trueName
                Map<String, String> trueNameGitName = getName(developerList);


                List<CloneMessage> gitNameClone = new ArrayList<>(developerList.size());
                developerList.forEach(d -> gitNameClone.add(getOneDeveloperCloneInfo(repositoryId, d, start, end, trueNameGitName.get(d))));

                Map<String, List<CloneMessage>> map = gitNameClone.parallelStream().collect(Collectors.groupingBy(CloneMessage::getDeveloper));
                map.values().forEach(c -> cloneMessages.add(union(c)));
            } else {
                List<String> gitName = repoCommitMapper.getAllGitName(developer);
                gitName.forEach(n -> cloneMessages.add(getOneDeveloperCloneInfo(repositoryId, n, start, end, developer)));
                CloneMessage u = union(cloneMessages);
                cloneMessages.clear();
                cloneMessages.add(u);
            }
        }
        return cloneMessages;
    }

    /**
     * fixme 目前先简单相加 后面从入库的时候就考虑人员聚合
     */
    private CloneMessage union(List<CloneMessage> cloneMessageList) {
        if (cloneMessageList == null || cloneMessageList.size() == 0) {
            return null;
        }
        CloneMessage result = cloneMessageList.get(0);
        for (int i = 1; i < cloneMessageList.size(); i++) {

            CloneMessage c = cloneMessageList.get(i);
            result.setIncreasedCloneLines(result.getIncreasedCloneLines() + c.getIncreasedCloneLines());
            result.setSelfIncreasedCloneLines(result.getSelfIncreasedCloneLines() + c.getSelfIncreasedCloneLines());
            result.setEliminateCloneLines(result.getEliminateCloneLines() + c.getEliminateCloneLines());

            if (! result.getRepoId().equals(c.getRepoId())) {
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
            String name = m.get("accountName");
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
        String [] targetRepos  = new String[0];
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
            for(String commitId : repoCommitList){
                for(CloneMeasure cloneMeasure1 : cloneMeasures){
                    //计算个人
                    if(cloneMeasure1.getCommitId().equals(commitId) && developerCommitList.contains(commitId)){
                        newCloneLines += cloneMeasure1.getNewCloneLines();
                        selfCloneLines += cloneMeasure1.getSelfCloneLines();
                        if(preCloneLines > cloneMeasure1.getCloneLines()){
                            deleteCloneLines += preCloneLines - cloneMeasure1.getCloneLines();
                        }
                    }
                    //计算全体
                    if(cloneMeasure1.getCommitId().equals(commitId)){
                        //消除clone行数,只计算消除
                        if(preCloneLines > cloneMeasure1.getCloneLines()){
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
        cloneMessage.setDeveloper(trueName);
        if (StringUtils.isEmpty(trueName)) {
            cloneMessage.setDeveloper(developer);
        }
        cloneMessage.setRepoId(repositoryId);
        cloneMessage.setIncreasedCloneLines(newCloneLines);
        cloneMessage.setSelfIncreasedCloneLines(selfCloneLines);
        cloneMessage.setAddLines(addLines);
        cloneMessage.setEliminateCloneLines(deleteCloneLines);
        cloneMessage.setAllEliminateCloneLines(allDeleteCloneLines);

        return cloneMessage;
    }

//    @Override
//    public void deleteCloneMeasureByRepoId(String repoId) {
//        cloneMeasureDao.deleteCloneMeasureByRepoId(repoId);
//        cloneInfoDao.deleteCloneInfo(repoId);
//    }

    /**
     * 获取某个版本的clone行数
     * 计算的方式有待商榷
     *
     * @param repoId   repo id
     * @param commitId commit id
     * @return 行数
     */
    private int getCloneLines(String repoId, String commitId) {

        int cloneLinesWithOutTest = 0;
        if (commitId != null) {
            List<CloneLocation> cloneLocations = cloneLocationDao.getCloneLocations(repoId, commitId);
            Map<String, List<String>> locationMap = new HashMap<>(512);
            for (CloneLocation location : cloneLocations) {
                String[] nums = location.getNum().split(",");
                for (String line : nums) {
                    locationMap = ComputeUtil.putNewNum(locationMap, line, location.getFilePath());
                }
            }
            log.info("repoId:{} - commitId:{}--->cloneLines:{}", repoId, commitId, cloneLinesWithOutTest);
            return ComputeUtil.getCloneLines(locationMap);
        }

        return -1;
    }

    @Override
    public void insertCloneMeasure(String repoId, String commitId, String repoPath) {
        //如果数据库中有对应的信息则直接返回
        if (cloneMeasureDao.getCloneMeasureCount(repoId, commitId) > 0) {
            return;
        }
        int increasedLines;
        int currentCloneLines;
        Map<String, String> map;
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
        //初始化
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
        //key记录repoPath, value记录新增且是clone的行号
        Map<String, String> addCloneLocationMap;
        //key记录repoPath, value记录新增且是self clone的行号
        Map<String, String> selfCloneLocationMap;

        //todo
        CloneMeasure cloneMeasure = forkJoinRecursiveTask.extract(repoId, commitId, repoPath, cloneLocations, cloneLocationMap, map);
        if (cloneMeasure == null) {
            return;
        }

        addCloneLocationMap = cloneMeasure.getAddCloneLocationMap();
        selfCloneLocationMap = cloneMeasure.getSelfCloneLocationMap();
        //插入cloneInfo
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
        String [] targetRepos  = new String[0];
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

    @Autowired
    public CloneMeasureServiceImpl(RepoCommitMapper repoCommitMapper, RestInterfaceManager restInterfaceManager, CloneMeasureDao cloneMeasureDao, CloneInfoDao cloneInfoDao, CloneLocationDao cloneLocationDao, ForkJoinRecursiveTask forkJoinRecursiveTask, CloneInfoMapper cloneInfoMapper) {
        this.repoCommitMapper = repoCommitMapper;
        this.restInterfaceManager = restInterfaceManager;
        this.cloneMeasureDao = cloneMeasureDao;
        this.cloneInfoDao = cloneInfoDao;
        this.cloneLocationDao = cloneLocationDao;
        this.forkJoinRecursiveTask = forkJoinRecursiveTask;
        this.cloneInfoMapper = cloneInfoMapper;
    }


}
