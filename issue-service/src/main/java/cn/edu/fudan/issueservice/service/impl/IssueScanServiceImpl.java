package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.config.ScanThreadExecutorConfig;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dto.ScanCommitInfoDTO;
import cn.edu.fudan.issueservice.domain.enums.RepoNatureEnum;
import cn.edu.fudan.issueservice.service.IssueScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * -【【lsw
 *
 * @author lsw
 */
@Service
@Slf4j
public class IssueScanServiceImpl implements IssueScanService {

    private BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue;
    private StringRedisTemplate stringRedisTemplate;
    private IssueScanDao issueScanDao;
    private IssueRepoDao issueRepoDao;
    private CommitDao commitDao;

    private static final String TOTAL = "total";

    @Override
    public void stopScan(String repoId, String toolName) {
        ScanCommitInfoDTO scanCommitInfoDTO = new ScanCommitInfoDTO();
        scanCommitInfoDTO.setRepoId(repoId);
        scanCommitInfoDTO.setToolName(toolName);
        boolean result = false;
        //todo 1. 直接调用BlockingQueue的 remove方法，如果删除成功返回true ，如果不存在则返回false
        result = scanCommitInfoDTOBlockingQueue.remove(scanCommitInfoDTO);
        //todo 此处采用抛出异常的方式还是返回布尔值有待权衡
        if (result) {
            return;
        }

        //2. 判断该项目是否已经在扫描
        // 此处因为在判断的时候，没有上锁，可能会存在判断的时候正在扫描，但是清除的时候项目已经扫描完成了，此时调用interrupt会影响到其他项目的扫描。
        // 所以加了开关跟interrupt双验证
        String threadName = stringRedisTemplate.opsForValue().get(repoId + "-" + toolName);
        if (threadName != null) {
            Thread scanThread = getThreadByThreadName(threadName);
            if (scanThread != null) {
                scanThread.interrupt();
                ScanThreadExecutorConfig.setConsumerThreadSwitch(repoId, false, toolName);
            }
        }
    }

    @Override
    public IssueRepo getScanStatus(String repoId, String toolName) throws Exception {
        List<IssueRepo> issueRepos = issueRepoDao.getIssueRepoByCondition(repoId, null, toolName);
        //三种情况
        if (issueRepos == null || issueRepos.isEmpty()) {
            //第一种还未扫描
            return null;
        } else if (issueRepos.size() == 1) {
            return issueRepos.get(0);
        } else {
            IssueRepo updateIssueRepo = null;
            for (IssueRepo issueRepo : issueRepos) {
                if (RepoNatureEnum.UPDATE.getType().equals(issueRepo.getNature())) {
                    updateIssueRepo = issueRepo;
                }
            }
            return updateIssueRepo;
        }
    }

    @Override
    public Map<String, Object> getCommitsCount(String repoUuid, String tool) {
        AtomicInteger notScanCommitCount = new AtomicInteger();

        List<HashMap<String, Integer>> notScanCommitsInfos = issueRepoDao.getNotScanCommitsCount(repoUuid, tool);

        //fixme 此处不应该是issue_repo表所有记录的差值的和，而应该是只看最新一条记录的差值
        notScanCommitsInfos.forEach(notScanCommitsInfo -> notScanCommitCount.addAndGet(notScanCommitsInfo.get("total_commit_count") - notScanCommitsInfo.get("scanned_commit_count")));

        return new HashMap<String, Object>(8) {{
            put(TOTAL, notScanCommitCount);
        }};
    }

    @Override
    public Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool) {

        Set<String> scannedCommitList = issueScanDao.getScannedCommitList(repoUuid, tool);

        List<Commit> wholeCommits = commitDao.getCommits(repoUuid, null);

        if (isWhole) {
            wholeCommits.forEach(commit -> commit.setScanned(scannedCommitList.contains(commit.getCommitId())));
            return new HashMap<String, Object>(8) {{
                put(TOTAL, wholeCommits.size());
                put("commitList", wholeCommits.subList((page - 1) * size, Math.min(page * size, wholeCommits.size())));
                put("pageCount", wholeCommits.size() % size != 0 ? wholeCommits.size() / size + 1 : wholeCommits.size() / size);
            }};
        }
        IssueRepo mainIssueRepo = issueRepoDao.getMainIssueRepo(repoUuid, tool);

        List<Commit> commits = commitDao.getCommits(repoUuid, mainIssueRepo == null ? null :
                commitDao.getCommitByCommitId(repoUuid, mainIssueRepo.getStartCommit()).getCommitTime().substring(0, 19));

        commits.removeIf(commit -> scannedCommitList.contains(commit.getCommitId()));

        return new HashMap<String, Object>(8) {{
            put(TOTAL, commits.size());
            put("commitList", commits.subList((page - 1) * size, Math.min(page * size, commits.size())));
            put("pageCount", commits.size() % size != 0 ? commits.size() / size + 1 : commits.size() / size);
        }};
    }

    private Thread getThreadByThreadName(String threadName) {
        Thread result = null;
        if (threadName == null) {
            return null;
        }
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (threadName.equals(lstThreads[i].getName())) {
                result = lstThreads[i];
            }
        }
        return result;
    }

    @Autowired
    public void setScanCommitInfoDTOBlockingQueue(BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue) {
        this.scanCommitInfoDTOBlockingQueue = scanCommitInfoDTOBlockingQueue;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
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
