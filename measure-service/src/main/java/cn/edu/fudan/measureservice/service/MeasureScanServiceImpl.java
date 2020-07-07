package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.analyzer.JavaNcss;
import cn.edu.fudan.measureservice.annotation.RepoResource;
import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.Objects;
import cn.edu.fudan.measureservice.domain.core.FileMeasure;
import cn.edu.fudan.measureservice.domain.core.MeasureScan;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.MeasureScanMapper;
import cn.edu.fudan.measureservice.mapper.PackageMeasureMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.FileFilter;
import cn.edu.fudan.measureservice.util.JGitHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
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
    private PackageMeasureMapper packageMeasureMapper;
    private FileMeasureMapper fileMeasureMapper;
    private MeasureScanMapper measureScanMapper;
    private ThreadLocal<JGitHelper> jGitHelperT = new ThreadLocal<>();


    public MeasureScanServiceImpl(RepoMeasureMapper repoMeasureMapper, PackageMeasureMapper packageMeasureMapper, FileMeasureMapper fileMeasureMapper, MeasureScanMapper measureScanMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
        this.packageMeasureMapper = packageMeasureMapper;
        this.fileMeasureMapper = fileMeasureMapper;
        this.measureScanMapper = measureScanMapper;
    }

    @Override
    public Object getScanStatus(String repoId) {
        List<Map<String, Object>> result = measureScanMapper.getScanStatus(repoId);
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
        String repoId = repoResource.getRepoId();
        if (StringUtils.isEmpty(repoPath)){
            log.error("repoId:[{}] path is empty", repoId);
            return;
        }
        //1. 判断beginCommit是否为空,为空则表示此次为update，不为空表示此次为第一次扫描
        // 若是update，则获取最近一次扫描的commit_id，作为本次扫描的起始点
        boolean isUpdate = false;
        if (StringUtils.isEmpty(beginCommit)){
            isUpdate = true;
            beginCommit = repoMeasureMapper.getLastScannedCommitId(repoId);
        }

        jGitHelperT.remove();
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        jGitHelperT.set(jGitHelper);

        // 获取从 beginCommit 开始的 commit list 列表
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, isUpdate);
        //初始化本次扫描状态信息
        Date startScanTime = new Date();
        MeasureScan measureScan = MeasureScan.builder()
                .uuid(UUID.randomUUID().toString()).repoId(repoId).tool(toolName)
                .startScanTime(startScanTime).endScanTime(startScanTime)
                .totalCommitCount(commitList.size()).scannedCommitCount(0)
                .scanTime(0).status(scanning)
                .startCommit(commitList.get(0)).endCommit(commitList.get(0)).build();
        measureScanMapper.insertOneMeasureScan(measureScan);

        int i = 1;
        // 遍历列表 进行扫描
        for (String commit : commitList) {
            log.info("Start to scan repoId is {} commit is {}", repoId, commit );
            String commitTime = jGitHelper.getCommitTime(commit);
            Measure measure = JavaNcss.analyse(repoPath);
            saveRepoLevelMeasureData(measure, repoId, commit, commitTime);
            saveFileMeasureData(repoId, commit, commitTime, measure.getObjects(), repoPath);
            //更新本次扫描状态信息
            Date currentTime = new Date();
            int scanTime = (int) (currentTime.getTime() - startScanTime.getTime()) / 1000;
            String status = i != commitList.size() ? scanning : complete;
            updateMeasureScan(measureScan, commit, i++, scanTime, status, currentTime);
        }

        log.info("Measure scan complete!");
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
    private void saveRepoLevelMeasureData(Measure measure,String repoId,String commitId,String commitTime){
        try{
            RepoMeasure repoMeasure=new RepoMeasure();
            repoMeasure.setUuid(UUID.randomUUID().toString());
            repoMeasure.setFiles(measure.getTotal().getFiles());
            repoMeasure.setNcss(measure.getTotal().getNcss());
            repoMeasure.setClasses(measure.getTotal().getClasses());
            repoMeasure.setFunctions(measure.getTotal().getFunctions());
            repoMeasure.setCcn(measure.getFunctions().getFunctionAverage().getNcss());
            repoMeasure.setJava_docs(measure.getTotal().getJavaDocs());
            repoMeasure.setJava_doc_lines(measure.getTotal().getJavaDocsLines());
            repoMeasure.setSingle_comment_lines(measure.getTotal().getSingleCommentLines());
            repoMeasure.setMulti_comment_lines(measure.getTotal().getMultiCommentLines());
            repoMeasure.setCommit_id(commitId);
            repoMeasure.setCommit_time(commitTime);
            repoMeasure.setRepo_id(repoId);


            JGitHelper jGitHelper = jGitHelperT.get();
            RevCommit revCommit = jGitHelper.getRevCommit(commitId);

            repoMeasure.setDeveloper_name(revCommit.getAuthorIdent().getName());
            repoMeasure.setDeveloper_email(revCommit.getAuthorIdent().getEmailAddress());
            repoMeasure.setCommit_message(revCommit.getShortMessage());

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

                List<String> changedFilePathList = jGitHelper.getChangedFilePathList(commitId);
                int changedFiles = 0;
                if (changedFilePathList != null) {
                    for (String s : changedFilePathList) {
                        if (! FileFilter.javaFilenameFilter(s)){
                            changedFiles++;
                        }
                    }
                }
                repoMeasure.setChanged_files(changedFiles);
            }



            try{
                if(repoMeasureMapper.sameMeasureOfOneCommit(repoId,commitId)==0) {
                    repoMeasureMapper.insertOneRepoMeasure(repoMeasure);
                    log.info("Successfully insert one record to repo_measure table ：repoId is " + repoId + " commitId is " + commitId);
                }
            } catch (Exception e) {
                log.error("Inserting data to DB repo_measure table failed：");
                e.printStackTrace();
            }

        } catch (Exception e) {
            log.error("Saving commit measure data failed：repoId is " + repoId + " commitId is " + commitId);
            e.printStackTrace();
        }

    }

//    /**
//     * 保存某个项目某个commit包级别的度量
//     */
//    private void savePackageMeasureData(Measure measure,String repoId,String commitId,String commitTime){
//        try{
//            List<Package> packages =new ArrayList<>();
//            DecimalFormat df=new DecimalFormat("#.00");
//            for(Package p:measure.getPackages().getPackages()){
//                p.setUuid(UUID.randomUUID().toString());
//                p.setCommit_id(commitId);
//                p.setCommit_time(commitTime);
//                p.setRepo_id(repoId);
//                if(packageMeasureMapper.samePackageMeasureExist(repoId,commitId,p.getName())>0) {
//                    continue;
//                }
//                String packageName=p.getName();
//                int count=0;
//                int ccn=0;
//                for(Function function:measure.getFunctions().getFunctions()){
//                    if(function.getName().startsWith(packageName)){
//                        count++;
//                        ccn+=function.getCcn();
//                    }
//                }
//                if(count==0) {
//                    p.setCcn(0.00);
//                } else{
//                    double result=(double)ccn/count;
//                    p.setCcn(Double.parseDouble(df.format(result)));
//                }
//                packages.add(p);
//            }
//            if(!packages.isEmpty()){
//                packageMeasureMapper.insertPackageMeasureDataList(packages);
//            }
//        } catch (NumberFormatException e) {
//            log.error("Saving package measure data failed：");
//            e.printStackTrace();
//        }
//
//    }


    /**
     * 保存某个项目某个commit文件级别的度量
     * fixme 未考虑rename的情况 目前先在jgitHelper {@link JGitHelper# getDiffEntry} 中修改了rename处理
     */
    @SneakyThrows
    private void saveFileMeasureData(String repoId, String commitId, String commitTime, Objects objects, String repoPath) {
        JGitHelper jGitHelper = jGitHelperT.get();

        //得到变更文件list
        List<String> changedFilePathList = jGitHelper.getChangedFilePathList(commitId);
        if (changedFilePathList == null) {
            return;
        }
        List<String> filePathList = new ArrayList<>(changedFilePathList.size());
        changedFilePathList.stream().filter(f -> !FileFilter.javaFilenameFilter(f)).forEach(filePathList::add);

        FileMeasure fileMeasure = new FileMeasure();
        fileMeasure.setRepoId(repoId);
        fileMeasure.setCommitId(commitId);
        fileMeasure.setCommitTime(commitTime);
        if (filePathList.size() == 0) {
            return;
        }
        List<FileMeasure> fileMeasureList = new ArrayList<>(filePathList.size());
        List<OObject> oObjects = objects.getObjects();
        Map<String, OObject> oObjectMap = new HashMap<>(oObjects.size() << 1);
        oObjects.forEach(o -> oObjectMap.put(o.getPath(), o));

        //获取本次commit所有文件的代码变更情况
        List<Map<String, Object>> fileLinesData = jGitHelper.getFileLinesData(commitId);
        for (String filePath : filePathList){
            fileMeasure.setUuid(UUID.randomUUID().toString());
            fileMeasure.setFilePath(filePath);
            int ccn = 0;
            int totalLine = 0;
            OObject oObject = oObjectMap.get(filePath);
            if (oObject != null) {
                ccn = oObject.getCcn();
                totalLine = oObject.getTotalLines();
            } else {
                log.error("OObject is null, path is {}", filePath);
            }
            fileMeasure.setCcn(ccn);
            fileMeasure.setTotalLine(totalLine);
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
            fileMeasure.setAddLine(addLines);
            fileMeasure.setDeleteLine(deleteLines);
            fileMeasureList.add(new FileMeasure(fileMeasure));
        }


        jGitHelper.checkout(jGitHelper.getSingleParent(commitId));
        for (FileMeasure f : fileMeasureList){
            String filePath = f.getFilePath();
            int preCcn = JavaNcss.getOneFileCcn(repoPath+'/'+filePath);
            fileMeasure.setDiffCcn(fileMeasure.getCcn() - preCcn);
        }

        try{
            fileMeasureMapper.insertFileMeasureList(fileMeasureList);
        } catch (Exception e) {
            log.error("Inserting data to DB file_measure table failed：");
            e.printStackTrace();
        }

    }
}