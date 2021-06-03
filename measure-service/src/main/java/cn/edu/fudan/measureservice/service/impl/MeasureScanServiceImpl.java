package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.annotation.RepoResource;
import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.core.ToolInvoker;
import cn.edu.fudan.measureservice.dao.AccountDao;
import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.core.MeasureScan;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.measureservice.domain.dto.ScanCommitInfoDto;
import cn.edu.fudan.measureservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.measureservice.mapper.*;
import cn.edu.fudan.measureservice.service.MeasureScanService;
import cn.edu.fudan.measureservice.util.JGitHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-11 10:48
 **/
@Slf4j
@Service
public class MeasureScanServiceImpl implements MeasureScanService {

    private ProjectMapper projectMapper;
    private ToolInvoker toolInvoker;
    private RepoMeasureMapper repoMeasureMapper;
    private MeasureScanMapper measureScanMapper;
    private RestInterfaceManager restInterfaceManager;
    private ThreadLocal<JGitHelper> jGitHelperT = new ThreadLocal<>();
    private ProjectDao projectDao;
    private AccountDao accountDao;
    private JiraDao jiraDao;
    private static final String SCANNING = "scanning";
    private static final String SCANNED = "complete";


    public MeasureScanServiceImpl(RepoMeasureMapper repoMeasureMapper, MeasureScanMapper measureScanMapper, ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
        this.repoMeasureMapper = repoMeasureMapper;
        this.measureScanMapper = measureScanMapper;
    }

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public Object getScanStatus(String repoUuid) {
        List<Map<String, Object>> result = measureScanMapper.getScanStatus(repoUuid);
        if(result.size()==0) {
            log.error("scan result is null");
            return null;
        }
        Map<String, Object> map = result.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
        String startScanTime = simpleDateFormat.format(map.get("startScanTime"));
        String endScanTime = simpleDateFormat.format(map.get("endScanTime"));
        map.put("startScanTime", startScanTime);
        map.put("endScanTime", endScanTime);
        return map;
    }



    @Override
    @RepoResource
    @SneakyThrows
    @Async("taskExecutor")
    // todo 未考虑多线程节点重复扫描问题，后续加入BlockingQueue<ScanCommitInfo>
    // todo 用 ScanCommitInfo包装后三个参数
    public synchronized void scan(RepoResourceDTO repoResource, String branch, String beginCommit) {
        String repoPath = repoResource.getRepoPath();
        String repoUuid = repoResource.getRepoUuid();
        if (StringUtils.isEmpty(repoPath)){
            log.error("repoUuid:[{}] path is empty", repoUuid);
            return;
        }
        try {
            String toolName = projectDao.getToolName(repoUuid);
            //1. 判断beginCommit是否为空,为空则表示此次为update，不为空表示此次为第一次扫描
            // 若是update，则获取最近一次扫描的commit_id，作为本次扫描的起始点
            boolean isUpdate = false;
            if (StringUtils.isEmpty(beginCommit)){
                isUpdate = true;
                beginCommit = repoMeasureMapper.getLastScannedCommitId(repoUuid);
            }
            jGitHelperT.remove();
            JGitHelper jGitHelper = new JGitHelper(repoPath);
            jGitHelperT.set(jGitHelper);

            // 获取从 beginCommit 开始的 commit list 列表
            List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, isUpdate);
            if (commitList.size() == 0){
                log.warn("The commitList is null. beginCommit is {}",beginCommit);
                return;
            }
            //todo 移至measureScanDao 初始化本次扫描状态信息
            Date startScanTime = new Date();
            MeasureScan measureScan = MeasureScan.builder()
                    .uuid(UUID.randomUUID().toString()).repoUuid(repoUuid).tool(toolName)
                    .startScanTime(startScanTime).endScanTime(startScanTime)
                    .totalCommitCount(commitList.size()).scannedCommitCount(0)
                    .scanTime(0).status(SCANNING)
                    .startCommit(commitList.get(0)).endCommit(commitList.get(0)).build();
            measureScanMapper.insertOneMeasureScan(measureScan);

            toolInvoker.setJGitHelper(jGitHelper);
            int i = 1;
            // 遍历列表 进行扫描
            for (String commit : commitList) {
                jGitHelper.checkout(commit);
                log.info("Start to scan repoUuid is {} commit is {}\n", repoUuid, commit );
                RevCommit revCommit = jGitHelper.getRevCommit(commit);
                String commitTime = jGitHelper.getCommitTime(revCommit);
                String authorName = jGitHelper.getAuthorName(revCommit);
                String mailAddress = jGitHelper.getAuthorEmailAddress(revCommit);
                String message = jGitHelper.getCommitMessage(revCommit);
                // 判断 message 是否包含 jira 单号
                int isCompliance = !"noJiraID".equals(jiraDao.getJiraIDFromCommitMsg(message)) ? 1 : 0;
                ScanCommitInfoDto scanCommitInfoDto = ScanCommitInfoDto.builder()
                        .commitId(commit)
                        .branch(branch)
                        .repoUuid(repoUuid)
                        .toolName(toolName)
                        .repoPath(repoPath)
                        .commitTime(commitTime)
                        .developerName(accountDao.getDeveloperName(authorName))
                        .mailAddress(mailAddress)
                        .message(message).isCompliance(isCompliance)
                        .build();

                toolInvoker.invoke(scanCommitInfoDto);
                if (!scanCommitInfoDto.getStatus().equals(ScanStatusEnum.DONE)) {
                    log.error("commit : {} , scan failed!\n",commit);
                }
                //更新本次扫描状态信息
                Date currentTime = new Date();
                int scanTime = (int) (currentTime.getTime() - startScanTime.getTime()) / 1000;
                String status = i != commitList.size() ? SCANNING : SCANNED;
                updateMeasureScan(measureScan, commit, i++, scanTime, status, currentTime);
            }
        }finally {
            log.info("free repo:{}, path:{}", repoUuid, repoPath);
            restInterfaceManager.freeRepoPath(repoUuid, repoPath);
        }
        log.info("Measure scan complete!");
    }

    @Override
    public void stop(String repoUuid) {
        // todo
    }

    @Override
    @SneakyThrows
    public void delete(String repoUuid) {
        log.info("measurement info start to delete");
        repoMeasureMapper.delRepoMeasureByrepoUuid(repoUuid);
        repoMeasureMapper.delFileMeasureByrepoUuid(repoUuid);
        log.info("measurement delete completed");
    }


    private void updateMeasureScan(MeasureScan measureScan, String endCommit, int scannedCommitCount,
                                          int scanTime, String status, Date endScanTime){
        measureScan.setEndCommit(endCommit);
        measureScan.setScannedCommitCount(scannedCommitCount);
        measureScan.setScanTime(scanTime);
        measureScan.setStatus(status);
        measureScan.setEndScanTime(endScanTime);
        measureScanMapper.updateMeasureScan(measureScan);
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Autowired
    public void setJiraDao(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    @Autowired
    public void setToolInvoker(ToolInvoker toolInvoker) {
        this.toolInvoker = toolInvoker;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}