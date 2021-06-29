package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.common.domain.po.scan.RepoScan;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
public interface IssueScanService {

    /**
     * 获取扫描状态
     *
     * @param repoId   repoId
     * @param toolName toolName
     * @return IssueRepo
     * @throws Exception Exception
     */
    RepoScan getScanStatus(String repoId, String toolName);

    /**
     * 获取未扫锚commit数量
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return Map<String, Object>
     */
    Map<String, Object> getCommitsCount(String repoUuid, String tool);

    /**
     * 获取未扫锚commit详细信息
     *
     * @param repoUuid repoUuid
     * @param page     page
     * @param size     size
     * @param isWhole  isWhole
     * @param tool     tool
     * @return Map<String, Object>
     */
    Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool);

    /**
     * 获取扫描失败的commit list
     *
     * @param repoUuid
     * @return
     */
    Map<String, String> getScanFailedCommitList(String repoUuid);
}
