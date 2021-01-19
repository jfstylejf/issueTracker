package cn.edu.fudan.cloneservice.service;

import cn.edu.fudan.cloneservice.domain.*;
import java.util.List;


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
    List<CloneMessage> getCloneMeasure(String repoId, String developers, String start, String end, String page, String size, Boolean isAsc, String order);

    /**
     * 获取最新版本的clone行数
     *
     * @param repoId repo id
     * @return
     */
    CloneMeasure getLatestCloneMeasure(String repoId);

    List<CloneMessage> sortByOrder(List<CloneMessage> cloneMessages, String order);

}
