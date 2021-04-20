package cn.edu.fudan.measureservice.core; 

import cn.edu.fudan.measureservice.core.process.BaseAnalyzer;
import cn.edu.fudan.measureservice.core.process.JsCodeAnalyzer;
import cn.edu.fudan.measureservice.domain.Measure;
import cn.edu.fudan.measureservice.domain.OObject;
import cn.edu.fudan.measureservice.domain.core.FileMeasure;
import cn.edu.fudan.measureservice.domain.dto.DiffInfo;
import cn.edu.fudan.measureservice.domain.dto.ScanCommitInfoDto;
import cn.edu.fudan.measureservice.domain.enums.ToolEnum;
import cn.edu.fudan.measureservice.mapper.FileMeasureMapper;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.util.*;
import lombok.SneakyThrows;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/** 
* ToolInvoker Tester. 
* 
* @author wjzho
* @since <pre>04/07/2021</pre> 
* @version 1.0 
*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class JsToolInvokerTest {

private ScanCommitInfoDto scanCommitInfoDto;
private BaseAnalyzer baseAnalyzer;
private JGitHelper jGitHelper;

@InjectMocks
private ToolInvoker toolInvoker;

@Mock
private RepoMeasureMapper repoMeasureMapper;

@Mock
private FileMeasureMapper fileMeasureMapper;


@Before
public void before() throws Exception {
    scanCommitInfoDto = ScanCommitInfoDto.builder()
            .commitId("82e965cbd0178dc512ae84e38f905d644ba934b6")
            .developerName("SunYuJie")
            .branch("dev")
            .repoPath("C:\\Users\\wjzho\\Desktop\\js_test\\fortestjs-davidtest_duplicate_fdse-0")
            .repoUuid("640c6a68-8df6-11eb-9988-b1d413682f00")
            .toolName("eslint")
            .build();
    baseAnalyzer = new JsCodeAnalyzer();
    baseAnalyzer.setScanCommitInfoDto(scanCommitInfoDto);
    baseAnalyzer.setLibHome(FileUtil.systemAvailablePath("C:\\\\Users\\\\wjzho\\\\Desktop\\\\js_test\\\\lib\\\\"));
    baseAnalyzer.setRepoPath(scanCommitInfoDto.getRepoPath());
    ((JsCodeAnalyzer) baseAnalyzer).setJsResultFileHome("C:\\\\Users\\\\wjzho\\\\Desktop\\\\js_test\\\\log");
    jGitHelper = new JGitHelper(scanCommitInfoDto.getRepoPath());
    toolInvoker.setJGitHelper(jGitHelper);
} 

@After
public void after() throws Exception { 
} 

/**
*
* Method: invoke(ScanCommitInfoDto scanCommitInfoDto)
*
*/
@Test
public void testInvoke() throws Exception {

}



/** 
* 
* Method: executeScan(BaseAnalyzer baseAnalyzer) 
* 
*/ 
@Test
public void testExecuteScan() throws Exception {
    boolean invokeToolResult = baseAnalyzer.invoke();
    Assert.assertTrue(invokeToolResult);
}

/** 
* 
* Method: executeAnalyse(BaseAnalyzer baseAnalyzer) 
* 
*/
@Test
public void testExecuteAnalyse() throws Exception {
    boolean analyseResult = baseAnalyzer.analyze();
    Assert.assertTrue(analyseResult);
} 

/**
*
* Method: insertMeasureData(ScanCommitInfoDto scanCommitInfoDto, Measure measure)
*
*/
@Test
public void testInsertMeasureData() throws Exception {
    List<DiffEntry> filteredDiffEntries = new ArrayList<>();
    List<DiffEntry> diffEntries = jGitHelper.getDiffEntry(scanCommitInfoDto.getCommitId());
    FileFilter fileFilter = new JsFileFilter();
    for (DiffEntry diffEntry : diffEntries) {
        if (!fileFilter.fileFilter(diffEntry.getNewPath()) || !fileFilter.fileFilter(diffEntry.getOldPath())) {
            filteredDiffEntries.add(diffEntry);
        }
    }


    //得到变更文件list
    Map<DiffEntry.ChangeType, List<String>> diffFilePathList = jGitHelper.getDiffFilePathList(diffEntries);

    List<String> filePaths = new ArrayList<>(10);
    if (diffFilePathList.containsKey(DiffEntry.ChangeType.MODIFY)){
        filePaths.addAll(diffFilePathList.get(DiffEntry.ChangeType.MODIFY));
    }
    if (diffFilePathList.containsKey(DiffEntry.ChangeType.ADD)){
        filePaths.addAll(diffFilePathList.get(DiffEntry.ChangeType.ADD));
    }


    List<DiffInfo> diffInfos = toolInvoker.getDiffTextInfo(diffEntries);
    DiffInfo totalDiffInfo = toolInvoker.getTotalDiffInfo(diffInfos);

    Assert.assertEquals("总修改文件数测试数量有误",filteredDiffEntries.size(),1);
    Assert.assertEquals("新增修改文件数量有误",filePaths.size(),1);
    Assert.assertEquals("新增行数有误",totalDiffInfo.getAddLines(),24);
    Assert.assertEquals("删除行数有误",totalDiffInfo.getDelLines(),8);
    Assert.assertEquals("新增注释行",totalDiffInfo.getAddCommentLines(),0);
    Assert.assertEquals("删除注释行",totalDiffInfo.getDelCommentLines(),1);


    testExecuteAnalyse();
    Measure measure = baseAnalyzer.getAnalyzedResult();
    List<OObject> oObjects = measure.getObjects().getObjects();
    Map<String, OObject> oObjectMap = new HashMap<>(oObjects.size() << 1);
    oObjects.forEach(o -> oObjectMap.put(o.getPath(), o));
    for (DiffInfo diffInfo : diffInfos) {
        if (!filePaths.contains(diffInfo.getFilePath())) {
            continue;
        }
        String filePath = diffInfo.getFilePath();
        OObject oObject = oObjectMap.get(filePath);
        if (oObject != null) {
            int totalLine = oObject.getTotalLines();
            Assert.assertEquals("总行数有误",totalLine,24);
        }
    }



}



/** 
* 
* Method: getPreFileCcn(ScanCommitInfoDto scanCommitInfoDto, String filePath) 
* 
*/ 
@Test
public void testGetPreFileCcn() throws Exception {

} 

/** 
* 
* Method: getTotalDiffInfo(List<DiffInfo> diffInfos) 
* 
*/ 
@Test
public void testGetTotalDiffInfo() throws Exception { 

} 

/** 
* 
* Method: getDiffTextInfo(List<DiffEntry> diffEntries) 
* 
*/ 
@Test
public void testGetDiffTextInfo() throws Exception { 

} 



/** 
* 
* Method: getSpecificFilter(String toolName) 
* 
*/ 
@Test
public void testGetSpecificFilter() throws Exception { 
    Assert.assertTrue("JsFilter match failed\n",toolInvoker.getSpecificFilter(ToolEnum.JSCodeAnalyzer.getType()) instanceof JsFileFilter);
    Assert.assertTrue("JavaFilter match failed\n",toolInvoker.getSpecificFilter(ToolEnum.JavaCodeAnalyzer.getType()) instanceof JavaFileFilter);
} 

} 
