package cn.edu.fudan.measureservice.core;


import cn.edu.fudan.measureservice.analyzer.JavaNcss;
import cn.edu.fudan.measureservice.core.process.BaseAnalyzer;
import cn.edu.fudan.measureservice.core.process.CppCodeAnalyzer;
import cn.edu.fudan.measureservice.core.process.JavaCodeAnalyzer;
import cn.edu.fudan.measureservice.core.process.JsCodeAnalyzer;
import cn.edu.fudan.measureservice.domain.Measure;
import cn.edu.fudan.measureservice.domain.OObject;
import cn.edu.fudan.measureservice.domain.Objects;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import cn.edu.fudan.measureservice.domain.Total;
import cn.edu.fudan.measureservice.domain.core.FileMeasure;
import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.ScanCommitInfoDto;
import cn.edu.fudan.measureservice.domain.dto.DiffInfo;
import cn.edu.fudan.measureservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.measureservice.domain.enums.ToolEnum;
import cn.edu.fudan.measureservice.filter.FileFilter;
import cn.edu.fudan.measureservice.filter.JavaFileFilter;
import cn.edu.fudan.measureservice.filter.JsFileFilter;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * @author wjzho
 */
@Slf4j
@Service
public class ToolInvoker {

    private JGitHelper jGitHelper;
    private RepoMeasureMapper repoMeasureMapper;
    private FileMeasureMapper fileMeasureMapper;

    @Value("${binHome}")
    protected String binHome;

    @Value("${libHome}")
    protected String libHome;



    @SneakyThrows
    public void invoke(ScanCommitInfoDto scanCommitInfoDto) {
        BaseAnalyzer baseAnalyzer;
        //1.??????????????????
        if (ToolEnum.JavaCodeAnalyzer.getType().equals(scanCommitInfoDto.getToolName())) {
            baseAnalyzer = new JavaCodeAnalyzer();
        }else if (ToolEnum.JSCodeAnalyzer.getType().equals(scanCommitInfoDto.getToolName())) {
            baseAnalyzer = new JsCodeAnalyzer();
        }else if (ToolEnum.CppCodeAnalyzer.getType().equals(scanCommitInfoDto.getToolName())) {
            baseAnalyzer = new CppCodeAnalyzer();
        }else {
            log.error ("toolName is error , do not have {}-->", scanCommitInfoDto.getToolName());
            return ;
        }
        baseAnalyzer.setScanCommitInfoDto(scanCommitInfoDto);
        baseAnalyzer.setBinHome(FileUtil.systemAvailablePath(binHome));
        baseAnalyzer.setLibHome(FileUtil.systemAvailablePath(libHome));
        baseAnalyzer.setRepoPath(FileUtil.systemAvailablePath(scanCommitInfoDto.getRepoPath()));
        //2.????????????
        Boolean scanResult = executeScan(baseAnalyzer);
        if (!scanResult) {
            return;
        }
        Boolean analyseResult = executeAnalyse(baseAnalyzer);
        if(!analyseResult) {
            return;
        }
        baseAnalyzer.getScanCommitInfoDto().setStatus(ScanStatusEnum.DONE);
        Measure analyseReport = baseAnalyzer.getAnalyzedResult();
        if (!insertMeasureData(scanCommitInfoDto,analyseReport)) {
            log.error("insert measure data failed ! \n");
        }
    }

    private Boolean executeScan(BaseAnalyzer baseAnalyzer) {
        baseAnalyzer.getScanCommitInfoDto().setStatus(ScanStatusEnum.SCANNING);
        boolean invokeToolResult = baseAnalyzer.invoke();
        if (!invokeToolResult) {
            baseAnalyzer.getScanCommitInfoDto().setStatus(ScanStatusEnum.INVOKE_TOOL_FAILED);
            return false;
        }
        return true;
    }

    private Boolean executeAnalyse(BaseAnalyzer baseAnalyzer) {
        baseAnalyzer.getScanCommitInfoDto().setStatus(ScanStatusEnum.ANALYZING);
        boolean analyseResult = baseAnalyzer.analyze();
        if (!analyseResult) {
            baseAnalyzer.getScanCommitInfoDto().setStatus(ScanStatusEnum.ANALYZE_FAILED);
            return false;
        }
        return true;
    }


    @SneakyThrows
    private Boolean insertMeasureData(ScanCommitInfoDto scanCommitInfoDto,Measure measure) {
        List<DiffEntry> diffEntries = getFilteredFileDiff(scanCommitInfoDto.getCommitId(),scanCommitInfoDto.getToolName());
        List<String> diffFilePathList = getChangedFileList(diffEntries);
        if (!saveRepoLevelMeasureData(measure,scanCommitInfoDto,diffEntries,diffEntries.size())) {
            return false;
        }
        return saveFileMeasureData(scanCommitInfoDto, measure.getObjects(), diffEntries, diffFilePathList);

    }

    /**
     * ????????????????????????commit?????????????????????
     */
    private Boolean saveRepoLevelMeasureData(Measure measure,ScanCommitInfoDto scanCommitInfoDto,List<DiffEntry> diffEntries,int changedFileNumber){
        RevCommit revCommit = jGitHelper.getRevCommit(scanCommitInfoDto.getCommitId());
        Total total = measure.getTotal();
        // ??????????????????????????????
        RepoMeasure repoMeasure = RepoMeasure.builder().uuid(UUID.randomUUID().toString())
                .files(total.getFiles()).ncss(total.getNcss()).classes(total.getClasses())
                .functions(total.getFunctions()).ccn(measure.getFunctions().getFunctionAverage().getCcn())
                .java_docs(total.getJavaDocs()).java_doc_lines(total.getJavaDocsLines())
                .single_comment_lines(total.getSingleCommentLines()).multi_comment_lines(total.getMultiCommentLines())
                .commit_id(scanCommitInfoDto.getCommitId()).commit_time(scanCommitInfoDto.getCommitTime()).repo_id(scanCommitInfoDto.getRepoUuid())
                .developer_name(scanCommitInfoDto.getDeveloperName())
                .commit_message(scanCommitInfoDto.getMessage()).is_compliance(scanCommitInfoDto.getIsCompliance())
                .absoluteLines(total.getAbsoluteLines()).build();

        repoMeasure.set_merge(jGitHelper.isMerge(revCommit));

        int parentCount = revCommit.getParentCount();
        if (parentCount == 1) {
            scanCommitInfoDto.setFirstParentCommitId(revCommit.getParent(0).getId().getName());
        }else if (parentCount ==2 ) {
            scanCommitInfoDto.setFirstParentCommitId(revCommit.getParent(0).getId().getName());
            scanCommitInfoDto.setSecondParentCommitId(revCommit.getParent(1).getId().getName());
        }else {
            //todo rebase ???????????????
        }

        //???????????????????????????commit????????????????????????0?????????  ?????????git diff ?????????????????????
        if (parentCount == 0){
            repoMeasure.setAdd_lines(0);
            repoMeasure.setDel_lines(0);
            repoMeasure.setAdd_comment_lines(0);
            repoMeasure.setDel_comment_lines(0);
            repoMeasure.setChanged_files(0);
        }else{
            List<DiffInfo> diffInfos = getDiffTextInfo(diffEntries);
            DiffInfo totalDiffInfo = getTotalDiffInfo(diffInfos);
            repoMeasure.setAdd_lines(totalDiffInfo.getAddLines());
            repoMeasure.setDel_lines(totalDiffInfo.getDelLines());
            repoMeasure.setAdd_comment_lines(totalDiffInfo.getAddCommentLines());
            repoMeasure.setDel_comment_lines(totalDiffInfo.getDelCommentLines());
            repoMeasure.setChanged_files(changedFileNumber);
            repoMeasure.setFirst_parent_commit_id(scanCommitInfoDto.getFirstParentCommitId());
            repoMeasure.setSecond_parent_commit_id(scanCommitInfoDto.getSecondParentCommitId());
        }

        try{
            if(repoMeasureMapper.containCommitIdOrNot(scanCommitInfoDto.getCommitId())==0) {
                repoMeasureMapper.insertOneRepoMeasure(repoMeasure);
            }
        } catch (Exception e) {
            log.error("Inserting data to DB repo_measure table failed???");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * ????????????????????????commit?????????????????????
     * fixme ?????????rename????????? ????????????jgitHelper {@link JGitHelper# getDiffEntry} ????????????rename??????
     */
    @SneakyThrows
    private Boolean saveFileMeasureData(ScanCommitInfoDto scanCommitInfoDto,Objects objects,List<DiffEntry> diffEntries,List<String> filePaths) {
        if(filePaths.size()==0) {
            log.warn("no diffFile");
            return false;
        }
        List<FileMeasure> fileMeasureList = new ArrayList<>(filePaths.size());
        List<OObject> oObjects = objects.getObjects();
        Map<String, OObject> oObjectMap = new HashMap<>(oObjects.size() << 1);
        oObjects.forEach(o -> oObjectMap.put(o.getPath(), o));

        List<DiffInfo> diffInfos = getDiffTextInfo(diffEntries);
        for (DiffInfo diffInfo : diffInfos) {
            if (!filePaths.contains(diffInfo.getFilePath())) {
                continue;
            }
            int ccn = 0;
            int totalLine = 0;
            int absoluteLine = 0;
            String filePath = diffInfo.getFilePath();
            OObject oObject = oObjectMap.get(filePath);
            if (oObject != null) {
                ccn = oObject.getCcn();
                totalLine = oObject.getTotalLines();
                absoluteLine = oObject.getAbsoluteLines();
            } else {
                // fixme ???????????????????????? ???????????????
                log.error("OObject is null, filePath is {}", filePath);
            }
            int addLines = diffInfo.getAddLines();
            int deleteLines = diffInfo.getDelLines();
            FileMeasure fileMeasure = FileMeasure.builder().uuid(UUID.randomUUID().toString()).repoUuid(scanCommitInfoDto.getRepoUuid())
                    .commitId(scanCommitInfoDto.getCommitId()).commitTime(scanCommitInfoDto.getCommitTime())
                    .addLine(addLines).deleteLine(deleteLines).totalLine(totalLine)
                    .ccn(ccn).diffCcn(0).filePath(filePath)
                    .absoluteLine(absoluteLine).build();
            fileMeasureList.add(fileMeasure);
        }

        String preCommit = jGitHelper.getSingleParent(scanCommitInfoDto.getCommitId());
        RevCommit revCommit = jGitHelper.getRevCommit(preCommit);
        String preCommitTime = jGitHelper.getCommitTime(revCommit);
        String preAuthorName = jGitHelper.getAuthorName(revCommit);
        String preMailAddress = jGitHelper.getAuthorEmailAddress(revCommit);
        jGitHelper.checkout(preCommit);
        for (FileMeasure f : fileMeasureList){
            String filePath = f.getFilePath();
            ScanCommitInfoDto preScanCommitInfo = ScanCommitInfoDto.builder().commitId(preCommit)
                    .commitTime(preCommitTime)
                    .branch(scanCommitInfoDto.getBranch())
                    .developerName(preAuthorName)
                    .mailAddress(preMailAddress)
                    .repoPath(scanCommitInfoDto.getRepoPath())
                    .toolName(scanCommitInfoDto.getToolName())
                    .repoUuid(scanCommitInfoDto.getRepoUuid())
                    .build();
            int preCcn = getPreFileCcn(preScanCommitInfo,filePath);
            f.setDiffCcn(f.getCcn() - preCcn);
        }

        try{
            // fixme ???????????????
            if(fileMeasureMapper.sameFileMeasureOfOneCommit(scanCommitInfoDto.getRepoUuid(),scanCommitInfoDto.getCommitId())==0) {
                fileMeasureMapper.insertFileMeasureList(fileMeasureList);
            }
            return true;
        } catch (Exception e) {
            log.error("Inserting data to DB file_measure table failed???");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ??????????????? diffCcn (???????????????????????????)
     * @param filePath
     * @return
     */
     int getPreFileCcn(ScanCommitInfoDto scanCommitInfoDto,String filePath) {
        if (ToolEnum.JavaCodeAnalyzer.getType().equals(scanCommitInfoDto.getToolName())) {
            return JavaNcss.getOneFileCcn(scanCommitInfoDto.getRepoPath()+'/'+filePath);
        }else if (ToolEnum.JSCodeAnalyzer.getType().equals(scanCommitInfoDto.getToolName())) {
            JsCodeAnalyzer jsCodeAnalyzer = new JsCodeAnalyzer();
            jsCodeAnalyzer.setRepoPath(scanCommitInfoDto.getRepoPath());
            jsCodeAnalyzer.setLibHome(libHome);
            jsCodeAnalyzer.setBinHome(binHome);
            jsCodeAnalyzer.setScanCommitInfoDto(scanCommitInfoDto);
            FileInfo preFileInfo = jsCodeAnalyzer.getPreFileInfo(filePath);
            return preFileInfo.getFileCcn();
        }else {
            return 0;
        }

    }

    DiffInfo getTotalDiffInfo(List<DiffInfo> diffInfos) {
        int sumAddLines = 0;
        int sumDelLines = 0;
        int sumAddCommentLines = 0;
        int sumDelCommentLines = 0;
        int sumAddWhiteLines = 0;
        int sumDelWhiteLines = 0;
        for (DiffInfo diffInfo : diffInfos) {
            sumAddLines += diffInfo.getAddLines();
            sumDelLines += diffInfo.getDelLines();
            sumAddCommentLines += diffInfo.getAddCommentLines();
            sumDelCommentLines += diffInfo.getDelCommentLines();
            sumAddWhiteLines += diffInfo.getAddWhiteLines();
            sumDelWhiteLines += diffInfo.getDelWhiteLines();
        }
        return DiffInfo.builder()
                .addLines(sumAddLines)
                .delLines(sumDelLines)
                .addCommentLines(sumAddCommentLines)
                .delCommentLines(sumDelCommentLines)
                .addWhiteLines(sumAddWhiteLines)
                .delWhiteLines(sumDelWhiteLines)
                .build();
    }

    /**
     * ???????????? commit ?????????????????????
     * @param diffEntries
     * @return
     */
    @SneakyThrows
    List<DiffInfo> getDiffTextInfo(List<DiffEntry> diffEntries)  {
        if (diffEntries == null) {
            return new ArrayList<>();
        }
        List<DiffInfo> diffInfos = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        //???????????????????????????????????????????????????????????????WS????????????White Space
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(jGitHelper.getRepository());

        for (DiffEntry entry : diffEntries) {
            df.format(entry);
            String diffText = out.toString("UTF-8");
            String[] diffLines = diffText.split("\n");
            DiffInfo diffInfo = FileFilter.diffLineFilter(diffLines);
            String filePath = entry.getNewPath();

            // ???????????????????????????????????????????????????????????????????????????????????????
            FileHeader fileHeader = df.toFileHeader(entry);
            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
            int addSize = 0;
            int subSize = 0;
            for(HunkHeader hunkHeader:hunks){
                EditList editList = hunkHeader.toEditList();
                for(Edit edit : editList){
                    subSize += edit.getEndA()-edit.getBeginA();
                    addSize += edit.getEndB()-edit.getBeginB();
                }
            }
            int addLines = addSize - diffInfo.getAddCommentLines() - diffInfo.getAddWhiteLines();
            int delLines = subSize - diffInfo.getDelCommentLines() - diffInfo.getDelWhiteLines();
            diffInfo.setFilePath(FileUtil.systemAvailablePath(filePath));
            diffInfo.setAddLines(addLines);
            diffInfo.setDelLines(delLines);
            diffInfos.add(diffInfo);
            out.reset();
        }
        return diffInfos;
    }


    /**
     * ??????????????????????????????????????????   eslint : JS  , javancss : Java
     * @param commitId
     * @param toolName
     * @return
     */
    public List<DiffEntry> getFilteredFileDiff(String commitId, String toolName) {
        List<DiffEntry> filteredDiffEntries = new ArrayList<>();
        List<DiffEntry> diffEntries = jGitHelper.getDiffEntry(commitId);
        // ??????????????? commit ???????????????
        if (diffEntries == null) {
            return new ArrayList<>();
        }
        FileFilter fileFilter = getSpecificFilter(toolName);
        if (fileFilter == null) {
            return diffEntries;
        }
        for (DiffEntry diffEntry : diffEntries) {
            if (!fileFilter.fileFilter(diffEntry.getNewPath()) || !fileFilter.fileFilter(diffEntry.getOldPath())) {
                filteredDiffEntries.add(diffEntry);
            }
        }
        return filteredDiffEntries;
    }

    /**
     * ???????????? commit ??? parent ??????????????????
     * @param diffEntries
     * @return
     */
    private List<String> getChangedFileList(List<DiffEntry> diffEntries) {
        if(diffEntries.size() == 0) {
            return new ArrayList<>();
        }
        //??????????????????list
        Map<DiffEntry.ChangeType, List<String>> diffFilePathList = jGitHelper.getDiffFilePathList(diffEntries);

        List<String> filePaths = new ArrayList<>(10);
        if (diffFilePathList.containsKey(DiffEntry.ChangeType.MODIFY)){
            filePaths.addAll(diffFilePathList.get(DiffEntry.ChangeType.MODIFY));
        }else {
            log.warn("diffFilePathList doesn't contain DiffEntry.ChangeType.MODIFY\n");
        }
        if (diffFilePathList.containsKey(DiffEntry.ChangeType.ADD)){
            filePaths.addAll(diffFilePathList.get(DiffEntry.ChangeType.ADD));
        }else {
            log.warn("diffFilePathList doesn't contain DiffEntry.ChangeType.ADD\n");
        }
        return filePaths;
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param toolName
     * @return
     */
    public FileFilter getSpecificFilter(String toolName) {
        FileFilter fileFilter;
        if (toolName.equals(ToolEnum.JavaCodeAnalyzer.getType())) {
            fileFilter = new JavaFileFilter();
        }else if (toolName.equals(ToolEnum.JSCodeAnalyzer.getType())) {
            fileFilter = new JsFileFilter();
        }else {
            log.error("cannot find tool\n");
            return null;
        }
        return fileFilter;
    }


    public void setJGitHelper(JGitHelper jGitHelper) {
        this.jGitHelper = jGitHelper;
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Autowired
    public void setFileMeasureMapper(FileMeasureMapper fileMeasureMapper) {
        this.fileMeasureMapper = fileMeasureMapper;
    }

    public JGitHelper getjGitHelper() {
        return jGitHelper;
    }

    public void setjGitHelper(JGitHelper jGitHelper) {
        this.jGitHelper = jGitHelper;
    }
}
