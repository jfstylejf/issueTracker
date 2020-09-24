package cn.edu.fudan.cloneservice.thread;


import cn.edu.fudan.cloneservice.domain.CloneMeasure;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.util.ComputeUtil;
import cn.edu.fudan.cloneservice.util.JGitUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author zyh
 * @date 2020/6/11
 */
@Slf4j
@Component
public class ForkJoinRecursiveTask {

    /**
     * 定义分解任务的最大阈值,即将一个clone location list分解成若干个子list，每个list的最大值
     */
    private final static int MAX_THRESHOLD = 200;

    private ForkJoinPool forkJoinPool;

    @Autowired
    private void setForkJoinPool(ForkJoinPool forkJoinPool){
        this.forkJoinPool = forkJoinPool;
    }

    public CloneMeasure extract(String repoId, String commitId, String repoPath, List<CloneLocation> cloneLocations, Map<String, List<CloneLocation>> cloneLocationMap, Map<String, String> map){

        ForkJoinTask<CloneMeasure> future = forkJoinPool.submit(new CalculatedRecursiveTask(0, cloneLocations.size() - 1, repoId, commitId, repoPath, cloneLocations, cloneLocationMap, map));
        try {
            return future.get();
        } catch (Exception e) {
            log.error("ForkJoinTask get CloneMeasure error!");
            log.error(e.getMessage());
        }
        return null;
    }

    private class CalculatedRecursiveTask extends RecursiveTask<CloneMeasure>{

        /**
         * 初始start为0， end为clone locations 的size-1
         */
        private int start;
        private int end;
        String repoId;
        String commitId;
        String repoPath;
        List<CloneLocation> cloneLocations;
        Map<String, List<CloneLocation>> cloneLocationMap;
        Map<String, String> map;

        public CalculatedRecursiveTask(int start, int end, String repoId, String commitId, String repoPath, List<CloneLocation> cloneLocations, Map<String, List<CloneLocation>> cloneLocationMap, Map<String, String> map) {
            this.start = start;
            this.end = end;
            this.repoId = repoId;
            this.commitId = commitId;
            this.repoPath = repoPath;
            this.cloneLocations = cloneLocations;
            this.cloneLocationMap = cloneLocationMap;
            this.map = map;
        }

        private CloneMeasure forkJoin(String repoId, String commitId, String repoPath,
                                      Map<String, List<CloneLocation>> cloneLocationMap,
                                      Map<String, String> map){
            CloneMeasure cloneMeasure = new CloneMeasure();

            List<CloneLocation> locationList = getSubList(start, end);
            log.info("{} -> cloneLocation init success!", Thread.currentThread().getName());
            //key记录repoPath和克隆组id, value记录新增且是clone的行号，记录clone片段的信息,会存在重复
            Map<String, String> addCloneLocationMap = new HashMap<>(512);
            //key记录repoPath和克隆组id, value记录新增且是self clone的行号，记录clone片段的信息,会存在重复
            Map<String, String> selfCloneLocationMap = new HashMap<>(512);
            //用于统计实际的新增clone行数
            Map<String, List<String>> addMap = new HashMap<>(512);
            //用于统计实际的新增自重复clone行数
            Map<String, List<String>> selfMap = new HashMap<>(512);
            //遍历此版本所有的clone location
            for(CloneLocation cloneLocation : locationList){
                String cloneLines = cloneLocation.getCloneLines();
                int startLine = Integer.parseInt(cloneLines.split(",")[0]);
                int endLine = Integer.parseInt(cloneLines.split(",")[1]);
                for(String filePath: map.keySet()){
                    if(filePath.equals(cloneLocation.getFilePath())){

                        String[] lines = map.get(filePath).split(",");

                        for(int i = 0; i < lines.length; i++){
                            if(Integer.parseInt(lines[i]) >= startLine && Integer.parseInt(lines[i]) <= endLine){
                                List<CloneLocation> list = cloneLocationMap.get(cloneLocation.getCategory());
                                String category = cloneLocation.getCategory();
                                for(CloneLocation cloneLocation1 : list){
                                    if(cloneLocation1.equals(cloneLocation)){
                                        continue;
                                    }
                                    String filePath1 = cloneLocation1.getFilePath();
                                    String cloneLines1 = cloneLocation1.getCloneLines();
                                    if(JGitUtil.isSameDeveloperClone(repoPath, commitId, filePath1, cloneLines1)){
                                        selfMap = ComputeUtil.putNewNum(selfMap, lines[i], filePath);
                                        selfCloneLocationMap.merge(category + ":" +filePath, lines[i], (v1, v2) -> v1 + "," + v2);
                                        break;
                                    }
                                }
                                addMap = ComputeUtil.putNewNum(addMap, lines[i], filePath);
                                addCloneLocationMap.merge(category + ":" +filePath, lines[i], (v1, v2) -> v1 + "," + v2);
                            }
                        }
                        break;
                    }
                }
            }

            String uuid = UUID.randomUUID().toString();
            cloneMeasure.setUuid(uuid);
            cloneMeasure.setCommitId(commitId);
            cloneMeasure.setRepoId(repoId);
            cloneMeasure.setNewCloneLines(ComputeUtil.getCloneLines(addMap));
            cloneMeasure.setSelfCloneLines(ComputeUtil.getCloneLines(selfMap));
            cloneMeasure.setAddCloneLocationMap(addCloneLocationMap);
            cloneMeasure.setSelfCloneLocationMap(selfCloneLocationMap);
            return cloneMeasure;
        }

        private Map<String, String> mergeMap(Map<String, String> map1, Map<String, String> map2){
            Map<String, String> newMap = new HashMap<>(map1);

            map2.forEach((key, value) -> newMap.merge(key, value, (v1, v2) -> v1 + "," + v2));

            return newMap;
        }

        private List<CloneLocation> getSubList(int start, int end){
            List<CloneLocation> subList = new ArrayList<>();
            IntStream.rangeClosed(start,end).forEach(i -> subList.add(cloneLocations.get(i)));
            return subList;
        }

        private CloneMeasure mergeCloneMeasure(CloneMeasure cloneMeasure1, CloneMeasure cloneMeasure2){
            CloneMeasure cloneMeasure = new CloneMeasure();
            String uuid = UUID.randomUUID().toString();
            cloneMeasure.setUuid(uuid);
            cloneMeasure.setCommitId(commitId);
            cloneMeasure.setRepoId(repoId);
            cloneMeasure.setNewCloneLines(cloneMeasure1.getNewCloneLines() + cloneMeasure2.getNewCloneLines());
            cloneMeasure.setSelfCloneLines(cloneMeasure1.getSelfCloneLines() + cloneMeasure2.getSelfCloneLines());
            cloneMeasure.setAddCloneLocationMap(mergeMap(cloneMeasure1.getAddCloneLocationMap(), cloneMeasure2.getAddCloneLocationMap()));
            cloneMeasure.setSelfCloneLocationMap(mergeMap(cloneMeasure1.getSelfCloneLocationMap(), cloneMeasure2.getSelfCloneLocationMap()));
            return cloneMeasure;
        }

        @Override
        protected CloneMeasure compute() {
            if(end - start <= MAX_THRESHOLD){
                return forkJoin(repoId, commitId, repoPath, cloneLocationMap, map);
            }else {
                int mid = (start + end)/2;
                CalculatedRecursiveTask left = new CalculatedRecursiveTask(start, mid, repoId, commitId, repoPath, cloneLocations, cloneLocationMap, map);
                CalculatedRecursiveTask right = new CalculatedRecursiveTask(mid + 1, end, repoId, commitId, repoPath, cloneLocations, cloneLocationMap, map);
                invokeAll(left, right);
                CloneMeasure result1 = left.join();
                CloneMeasure result2 = right.join();
                //合并两个measure对象
                return mergeCloneMeasure(result1, result2);
            }

        }
    }


}
