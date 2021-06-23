package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.domain.vo.CommitVO;
import cn.edu.fudan.issueservice.service.IssueScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * -【【lsw
 *
 * @author lsw
 */
@Service
@Slf4j
public class IssueScanServiceImpl implements IssueScanService {

    private IssueScanDao issueScanDao;
    private IssueRepoDao issueRepoDao;
    private CommitDao commitDao;

    private static final String TOTAL = "total";

    @Override
    public RepoScan getScanStatus(String repoId, String toolName) {
        List<RepoScan> issueRepos = issueRepoDao.getIssueRepoByCondition(repoId, toolName);
        return issueRepos == null || issueRepos.isEmpty() ? null : issueRepos.get(0);
    }

    @Override
    public Map<String, Object> getCommitsCount(String repoUuid, String tool) {
        AtomicInteger notScanCommitCount = new AtomicInteger();

        List<HashMap<String, Integer>> notScanCommitsInfos = issueRepoDao.getNotScanCommitsCount(repoUuid, tool);

        //fixme 此处不应该是issue_repo表所有记录的差值的和，而应该是只看最新一条记录的差值
        notScanCommitsInfos.forEach(notScanCommitsInfo -> notScanCommitCount.addAndGet(notScanCommitsInfo.get("total_commit_count") - notScanCommitsInfo.get("scanned_commit_count")));

        return new HashMap<>(8) {{
            put(TOTAL, notScanCommitCount);
        }};
    }

    @Override
    public Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool) {

        Set<String> scannedCommitList = issueScanDao.getScannedCommitList(repoUuid, tool);

        List<Commit> wholeCommits = commitDao.getCommits(repoUuid, null);

        if (isWhole) {
            List<CommitVO> commitVOList = new ArrayList<>();
            Map<String, String> scanStatusInRepo = issueScanDao.getScanStatusInRepo(repoUuid);
            for (Commit commit : wholeCommits) {
                commit.setScanned(scannedCommitList.contains(commit.getCommitId()));
                commitVOList.add(new CommitVO(commit, scanStatusInRepo.get(commit.getCommitId())));
            }
            return new HashMap<>(8) {{
                put(TOTAL, commitVOList.size());
                put("commitList", commitVOList.subList((page - 1) * size, Math.min(page * size, commitVOList.size())));
                put("pageCount", commitVOList.size() % size != 0 ? commitVOList.size() / size + 1 : commitVOList.size() / size);
            }};
        }
        RepoScan mainIssueRepo = issueRepoDao.getMainIssueRepo(repoUuid, tool);

        List<Commit> commits = commitDao.getCommits(repoUuid, mainIssueRepo == null ? null :
                commitDao.getCommitByCommitId(repoUuid, mainIssueRepo.getStartCommit()).getCommitTime().substring(0, 19));

        commits.removeIf(commit -> scannedCommitList.contains(commit.getCommitId()));

        return new HashMap<>(8) {{
            put(TOTAL, commits.size());
            put("commitList", commits.subList((page - 1) * size, Math.min(page * size, commits.size())));
            put("pageCount", commits.size() % size != 0 ? commits.size() / size + 1 : commits.size() / size);
        }};
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }

}
