package cn.edu.fudan.cloneservice.service;

import cn.edu.fudan.cloneservice.domain.*;
import org.eclipse.jgit.lib.PersonIdent;

import java.util.List;
import java.util.Set;

/**
 * @author znj
 * @date 2020/5
 */
public interface CloneMeasureService {

    /**
     * 插入clone度量结果
     * @param repoId repo id
     * @param commitId commit id
     * @param repoPath  path
     * @return
     */
    void insertCloneMeasure(String repoId, String commitId, String repoPath);

    /**
     * 获取一段时间内人员度量
     * @param repoId repo id
     * @param developer name
     * @param start start time
     * @param end end time
     * @return list
     */
    List<CloneMessage> getCloneMeasure(String repoId, String developer, String start, String end);

    /**
     * 删除clone度量结果
     * @param repoId repo id
     */
    void deleteCloneMeasureByRepoId(String repoId);

    /**
     * 触发度量，异步，非多线程
     * @param repoId repo id
     * @param startCommitId start commit id
     */
    void scanCloneMeasure(String repoId, String startCommitId);

    /**
     * 获取最新版本的clone行数
     * @param repoId repo id
     * @return
     */
    CloneMeasure getLatestCloneMeasure(String repoId);

}
