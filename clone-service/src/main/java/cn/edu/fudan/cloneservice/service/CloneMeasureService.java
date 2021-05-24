package cn.edu.fudan.cloneservice.service;

import cn.edu.fudan.cloneservice.domain.*;
import java.util.List;
import java.util.Map;


/**
 * @author znj
 * @date 2020/5
 */
public interface CloneMeasureService {

    /**
     * 插入clone度量结果
     *
     * @param repoId   repo id
     * @param commitId commit id
     * @param repoPath path
     * @return
     */
    void insertCloneMeasure(String repoId, String commitId, String repoPath);

    /**
     * 获取一段时间内人员度量
     *
     * @param repoId    repo id
     * @param developers accountName
     * @param start     start time
     * @param end       end time
     * @return list
     */
    List<CloneMessage> getCloneMeasure(String repoId, String developers, String start, String end);

    /**
     * 获取最新版本的clone行数
     *
     * @param repoId repo id
     * @return
     */
    CloneMeasure getLatestCloneMeasure(String repoId);

    List<CloneMessage> sortByOrder(List<CloneMessage> cloneMessages, String order);

    /**
     *
     * @param projectId
     * @param since
     * @param until
     * @param interval
     * @param token
     * @return Map<projectId, Map<date, num>>
     */
    List<CloneGroupSum> getCloneGroupsSum(String projectId, String since, String until, String interval, String token);

    List<CloneOverallView> getCloneOverallViews(String projectId, String repoUuid, String until, String token);

    List<CloneDetail> getCloneDetails(String projectId, String groupId, String commitId, String token);

    List<CloneDetailOverall> getCloneDetailOverall(String projectId, String commitId, String repoUuid,String until, String token);

    List<CloneMessage> getCloneLine(String projectId, String repoUuid, String developers, String since, String until, String token);
}
