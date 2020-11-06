package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.analyzer.JavaNcss;
import cn.edu.fudan.measureservice.annotation.RepoResource;
import cn.edu.fudan.measureservice.domain.Objects;
import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.core.FileMeasure;
import cn.edu.fudan.measureservice.domain.core.MeasureScan;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.measureservice.mapper.AccountMapper;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.MeasureScanMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureScanService;
import cn.edu.fudan.measureservice.util.FileFilter;
import cn.edu.fudan.measureservice.util.JGitHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.DiffEntry;
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

    private RepoMeasureMapper repoMeasureMapper;
    private FileMeasureMapper fileMeasureMapper;
    private MeasureScanMapper measureScanMapper;
    private ThreadLocal<JGitHelper> jGitHelperT = new ThreadLocal<>();


    public MeasureScanServiceImpl(RepoMeasureMapper repoMeasureMapper, FileMeasureMapper fileMeasureMapper, MeasureScanMapper measureScanMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
        this.fileMeasureMapper = fileMeasureMapper;
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
    @Async("taskExecutor")
    public void scan(RepoResourceDTO repoResource, String branch, String beginCommit, String toolName) {
        final String scanning = "scanning";
        final String complete = "complete";
        String repoPath = repoResource.getRepoPath();
        String repoUuid = repoResource.getRepoUuid();
        if (StringUtils.isEmpty(repoPath)){
            log.error("repoUuid:[{}] path is empty", repoUuid);
            return;
        }
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
        //初始化本次扫描状态信息
        Date startScanTime = new Date();
        MeasureScan measureScan = MeasureScan.builder()
                .uuid(UUID.randomUUID().toString()).repoUuid(repoUuid).tool(toolName)
                .startScanTime(startScanTime).endScanTime(startScanTime)
                .totalCommitCount(commitList.size()).scannedCommitCount(0)
                .scanTime(0).status(scanning)
                .startCommit(commitList.get(0)).endCommit(commitList.get(0)).build();
        measureScanMapper.insertOneMeasureScan(measureScan);

        int i = 1;
        // 遍历列表 进行扫描
        for (String commit : commitList) {
            jGitHelper.checkout(commit);
            log.info("Start to scan repoUuid is {} commit is {}", repoUuid, commit );
            String commitTime = jGitHelper.getCommitTime(commit);
            Measure measure = JavaNcss.analyse(repoPath);
            saveRepoLevelMeasureData(measure, repoUuid, commit, commitTime);
            saveFileMeasureData(repoUuid, commit, commitTime, measure.getObjects(), repoPath);
            //更新本次扫描状态信息
            Date currentTime = new Date();
            int scanTime = (int) (currentTime.getTime() - startScanTime.getTime()) / 1000;
            String status = i != commitList.size() ? scanning : complete;
            updateMeasureScan(measureScan, commit, i++, scanTime, status, currentTime);
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

    /**
     * 保存某个项目某个commit项目级别的度量
     */
    private void saveRepoLevelMeasureData(Measure measure,String repoUuid,String commitId,String commitTime){
        JGitHelper jGitHelper = jGitHelperT.get();
        RevCommit revCommit = jGitHelper.getRevCommit(commitId);
        
        Total total = measure.getTotal();
        // 以下是人员聚合的入库
        RepoMeasure repoMeasure = RepoMeasure.builder().uuid(UUID.randomUUID().toString()).files(total.getFiles()).ncss(total.getNcss()).classes(total.getClasses())
                .functions(total.getFunctions()).ccn(measure.getFunctions().getFunctionAverage().getNcss())
                .java_docs(total.getJavaDocs()).java_doc_lines(total.getJavaDocsLines())
                .single_comment_lines(total.getSingleCommentLines()).multi_comment_lines(total.getMultiCommentLines())
                .commit_id(commitId).commit_time(commitTime).repo_id(repoUuid)
                .developer_name(accountMapper.getAccountName(revCommit.getAuthorIdent().getName()) == null ? revCommit.getAuthorIdent().getName() : accountMapper.getAccountName(revCommit.getAuthorIdent().getName())).developer_email(revCommit.getAuthorIdent().getEmailAddress())
                .commit_message(revCommit.getShortMessage()).build();
       /* // 以下是非人员聚合的入库
        RepoMeasure repoMeasure = RepoMeasure.builder().uuid(UUID.randomUUID().toString()).files(total.getFiles()).ncss(total.getNcss()).classes(total.getClasses())
                .functions(total.getFunctions()).ccn(measure.getFunctions().getFunctionAverage().getNcss())
                .java_docs(total.getJavaDocs()).java_doc_lines(total.getJavaDocsLines())
                .single_comment_lines(total.getSingleCommentLines()).multi_comment_lines(total.getMultiCommentLines())
                .commit_id(commitId).commit_time(commitTime).repo_id(repoUuid)
                .developer_name(revCommit.getAuthorIdent().getName()).developer_email(revCommit.getAuthorIdent().getEmailAddress())
                .commit_message(revCommit.getShortMessage()).build();*/

        boolean isMerge = false;
        int parentNum = revCommit.getParentCount();
        if (parentNum == 1){
            String firstParentCommitId = revCommit.getParent(0).getId().getName();
            repoMeasure.setFirst_parent_commit_id(firstParentCommitId);
        }
        if (parentNum == 2){
            String firstParentCommitId = revCommit.getParent(0).getId().getName();
            repoMeasure.setFirst_parent_commit_id(firstParentCommitId);
            String secondParentCommitId = revCommit.getParent(1).getId().getName();
            repoMeasure.setSecond_parent_commit_id(secondParentCommitId);
            isMerge = true;
        }
        repoMeasure.set_merge(isMerge);

        Map<String, Integer> map = jGitHelper.getLinesData(commitId);
        //如果是最初始的那个commit，那么工作量记为0，否则  则进行git diff 对比获取工作量
        if (parentNum == 0 || map == null){
            repoMeasure.setAdd_lines(0);
            repoMeasure.setDel_lines(0);
            repoMeasure.setAdd_comment_lines(0);
            repoMeasure.setDel_comment_lines(0);
            repoMeasure.setChanged_files(0);
        }else{
            repoMeasure.setAdd_lines(map.get("addLines"));
            repoMeasure.setDel_lines(map.get("delLines"));
            repoMeasure.setAdd_comment_lines(map.get("addCommentLines"));
            repoMeasure.setDel_comment_lines(map.get("delCommentLines"));

            Map<DiffEntry.ChangeType, List<String>> filePaths = jGitHelper.getDiffFilePathList(commitId);
            int changedFiles = 0;
            for (List<String> changedFilePathList : filePaths.values()){
                if (changedFilePathList != null) {
                    for (String s : changedFilePathList) {
                        if (! FileFilter.javaFilenameFilter(s)){
                            changedFiles++;
                        }
                    }
                }
            }

            repoMeasure.setChanged_files(changedFiles);
        }
        
        try{
            if(repoMeasureMapper.sameMeasureOfOneCommit(repoUuid,commitId)==0) {
                repoMeasureMapper.insertOneRepoMeasure(repoMeasure);
            }
        } catch (Exception e) {
            log.error("Inserting data to DB repo_measure table failed：");
            e.printStackTrace();
        }
            

    }

    /**
     * 保存某个项目某个commit文件级别的度量
     * fixme 未考虑rename的情况 目前先在jgitHelper {@link JGitHelper# getDiffEntry} 中修改了rename处理
     */
    @SneakyThrows
    private void saveFileMeasureData(String repoUuid, String commitId, String commitTime, Objects objects, String repoPath) {
        JGitHelper jGitHelper = jGitHelperT.get();

        //得到变更文件list
        Map<DiffEntry.ChangeType, List<String>> diffFilePathList = jGitHelper.getDiffFilePathList(commitId);

        List<String> filePaths = new ArrayList<>(10);
        if (diffFilePathList.containsKey(DiffEntry.ChangeType.MODIFY)){
            diffFilePathList.get(DiffEntry.ChangeType.MODIFY).stream().filter(f -> !FileFilter.javaFilenameFilter(f)).forEach(filePaths::add);
        }else {
            log.warn("diffFilePathList doesn't contain DiffEntry.ChangeType.MODIFY");
        }
        if (diffFilePathList.containsKey(DiffEntry.ChangeType.ADD)){
            diffFilePathList.get(DiffEntry.ChangeType.ADD).stream().filter(f -> !FileFilter.javaFilenameFilter(f)).forEach(filePaths::add);
        }else {
            log.warn("diffFilePathList doesn't contain DiffEntry.ChangeType.ADD");
        }

        if (filePaths.size() == 0) {
            return;
        }
        List<FileMeasure> fileMeasureList = new ArrayList<>(filePaths.size());
        List<OObject> oObjects = objects.getObjects();
        Map<String, OObject> oObjectMap = new HashMap<>(oObjects.size() << 1);
        oObjects.forEach(o -> oObjectMap.put(o.getPath(), o));

        //获取本次commit所有文件的代码变更情况
        List<Map<String, Object>> fileLinesData = jGitHelper.getFileLinesData(commitId);
        for (String filePath : filePaths){
            int ccn = 0;
            int totalLine = 0;
            OObject oObject = oObjectMap.get(filePath);
            if (oObject != null) {
                ccn = oObject.getCcn();
                totalLine = oObject.getTotalLines();
            } else {
                // fixme 少量情况下会判空 有时间在看
                log.error("OObject is null, filePath is {}", filePath);
            }
            int addLines = 0;
            int deleteLines = 0;
            //根据filePath，获取对应文件的代码行变动情况
            for (Map<String, Object> fileLinesDatum : fileLinesData) {
                if (fileLinesDatum.get("filePath").equals(filePath)) {
                    addLines = Integer.parseInt(fileLinesDatum.get("addLines").toString());
                    deleteLines = Integer.parseInt(fileLinesDatum.get("delLines").toString());
                    break;
                }
            }

            FileMeasure fileMeasure = FileMeasure.builder().uuid(UUID.randomUUID().toString()).repoUuid(repoUuid).commitId(commitId).commitTime(commitTime)
                    .addLine(addLines).deleteLine(deleteLines).totalLine(totalLine)
                    .ccn(ccn).diffCcn(0).filePath(filePath).build();
            fileMeasureList.add(fileMeasure);
        }

        jGitHelper.checkout(jGitHelper.getSingleParent(commitId));
        for (FileMeasure f : fileMeasureList){
            String filePath = f.getFilePath();
            int preCcn = JavaNcss.getOneFileCcn(repoPath+'/'+filePath);
            f.setDiffCcn(f.getCcn() - preCcn);
        }

        try{
            fileMeasureMapper.insertFileMeasureList(fileMeasureList);
        } catch (Exception e) {
            log.error("Inserting data to DB file_measure table failed：");
            e.printStackTrace();
        }

    }
}