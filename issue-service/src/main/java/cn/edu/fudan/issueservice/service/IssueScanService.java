package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;

import java.util.Map;

/**
 * @author Beethoven
 */
public interface IssueScanService {

    /**
     * 准备扫描
     * @param repoResourceDTO repoResourceDTO
     * @param branch branch
     * @param beginCommit beginCommit
     * @param toolName toolName
     * @return String
     */
    String prepareForScan(RepoResourceDTO repoResourceDTO, String branch, String beginCommit, String toolName);

    /**
     * 停止扫描
     * @param repoId repoId
     * @param toolName toolName
     */
    void stopScan(String  repoId, String toolName);

    /**
     * 获取扫描状态
     * @param repoId repoId
     * @param toolName toolName
     * @return IssueRepo
     * @throws Exception  Exception
     */
    IssueRepo getScanStatus(String  repoId, String toolName) throws Exception;

    /**
     * 获取未扫锚commit数量
     * @param repoUuid repoUuid
     * @param tool tool
     * @return Map<String, Object>
     */
    Map<String, Object> getCommitsCount(String repoUuid, String tool);

    /**
     * 获取未扫锚commit详细信息
     * @param repoUuid repoUuid
     * @param page page
     * @param size size
     * @param isWhole isWhole
     * @param tool tool
     * @return Map<String, Object>
     */
    Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool);
}
