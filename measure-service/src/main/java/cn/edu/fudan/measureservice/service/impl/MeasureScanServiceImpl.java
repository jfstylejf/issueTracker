package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.annotation.RepoResource;
import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.core.ToolInvoker;
import cn.edu.fudan.measureservice.dao.AccountDao;
import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.dao.MeasureScanDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.core.MeasureScan;
import cn.edu.fudan.measureservice.domain.dto.*;
import cn.edu.fudan.measureservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.measureservice.mapper.*;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Lexer;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Parser;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CppExtractListener;
import cn.edu.fudan.measureservice.service.MeasureScanService;
import cn.edu.fudan.measureservice.util.JGitHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
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
    private MeasureScanDao measureScanDao;
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
        //???????????????timeStamp/dateTime??????????????????????????????????????? map.get("commit_time") ????????????????????????dateTime??????
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
    // todo ?????????????????????????????????????????????????????????BlockingQueue<ScanCommitInfo>
    // todo ??? ScanCommitInfo?????????????????????
    public synchronized void scan(RepoResourceDTO repoResource, String branch, String beginCommit) {
        String repoPath = repoResource.getRepoPath();
        String repoUuid = repoResource.getRepoUuid();
        if (StringUtils.isEmpty(repoPath)){
            log.error("repoUuid:[{}] path is empty", repoUuid);
            return;
        }
        try {
            String toolName = projectDao.getToolName(repoUuid);
            //1. ??????beginCommit????????????,????????????????????????update??????????????????????????????????????????
            // ??????update?????????????????????????????????commit_id?????????????????????????????????
            boolean isUpdate = false;
            if (StringUtils.isEmpty(beginCommit)){
                isUpdate = true;
                beginCommit = repoMeasureMapper.getLastScannedCommitId(repoUuid);
                //????????????????????????????????????????????? scan ????????? startCommit
                beginCommit = beginCommit == null ? measureScanDao.getRepoStartCommit(repoUuid) : beginCommit;
            }
            jGitHelperT.remove();
            JGitHelper jGitHelper = new JGitHelper(repoPath);
            jGitHelperT.set(jGitHelper);

            // ????????? beginCommit ????????? commit list ??????
            List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, isUpdate);
            if (commitList.size() == 0){
                log.warn("The commitList is null. beginCommit is {}",beginCommit);
                return;
            }
            //todo ??????measureScanDao ?????????????????????????????????
            Date startScanTime = new Date();
            // fixme ?????? ??????measureScan??????????????????
            MeasureScan measureScan = MeasureScan.builder()
                    .uuid(UUID.randomUUID().toString()).repoUuid(repoUuid).tool(toolName)
                    .startScanTime(startScanTime).endScanTime(startScanTime)
                    .totalCommitCount(commitList.size()).scannedCommitCount(0)
                    .scanTime(0).status(SCANNING)
                    .startCommit(commitList.get(0)).endCommit(commitList.get(0)).build();
            measureScanMapper.insertOneMeasureScan(measureScan);

            toolInvoker.setJGitHelper(jGitHelper);
            int i = 1;
            // ???????????? ????????????
            for (String commit : commitList) {
                jGitHelper.checkout(commit);
                log.info("Start to scan repoUuid is {} commit is {}\n", repoUuid, commit );
                RevCommit revCommit = jGitHelper.getRevCommit(commit);
                String commitTime = jGitHelper.getCommitTime(revCommit);
                String authorName = jGitHelper.getAuthorName(revCommit);
                String mailAddress = jGitHelper.getAuthorEmailAddress(revCommit);
                String message = jGitHelper.getCommitMessage(revCommit);
                // ?????? message ???????????? jira ??????
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
                //??????????????????????????????
                Date currentTime = new Date();
                int scanTime = (int) (currentTime.getTime() - startScanTime.getTime()) / 1000;
                String status = i != commitList.size() ? SCANNING : SCANNED;
                updateMeasureScan(measureScan, commit, i++, scanTime, status, currentTime);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
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


    public FileInfo parseFileInfo(String filePath) throws IOException {
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromFileName(filePath));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CPP14Parser parser = new CPP14Parser(tokens);

        ParseTree tree = parser.translationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();

        CppExtractListener listener = new CppExtractListener(parser);

        walker.walk(listener,tree);

        List<ParameterPair> memberList = listener.getMemberList();

        List<MethodInfo> methodInfos = listener.getMethodInfoList();

        FileInfo fileInfo = FileInfo.builder()
                .methodInfoList(methodInfos)
                .memberList(memberList)
                .absolutePath(filePath)
                .build();

        return fileInfo;
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
    public void setMeasureScanDao(MeasureScanDao measureScanDao) {
        this.measureScanDao = measureScanDao;
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