package cn.edu.fudan.measureservice.service; 

import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.dto.Query;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

/** 
* MeasureDeveloperService Tester. 
* 
* @author wjzho 
* @since <pre>06/25/2021</pre> 
* @version 1.0 
*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MeasureDeveloperServiceTest {

    private MeasureDeveloperService measureDeveloperService;
    private String token = "ec15d79e36e14dd258cfff3d48b73d35";


@Before
public void before() throws Exception {

} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getDeveloperWorkLoad(Query query, String developers) 
* 
*/ 
@Test
public void testGetDeveloperWorkLoad() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperWorkLoadWithLevel(Query query) 
* 
*/ 
@Test
public void testGetDeveloperWorkLoadWithLevel() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getStatementByCondition(String repoUuidList, String developer, String since, String until) 
* 
*/ 
@Test
public void testGetStatementByCondition() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperMetrics(String repoUuid, String developer, String since, String until, String token, String tool) 
* 
*/ 
@Test
public void testGetDeveloperMetrics() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getPortraitCompetence(String developer, String repoUuidList, String since, String until, String token) 
* 
*/ 
@Test
public void testGetPortraitCompetence() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperRecentNews(String repoUuids, String developer, String since, String until) 
* 
*/ 
@Test
public void testGetDeveloperRecentNews() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperPortrait(Query query) 
* 
*/ 
@Test
public void testGetDeveloperPortrait() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperRepositoryMetric(String developer, String repoUuid, String token) 
* 
*/ 
@Test
public void testGetDeveloperRepositoryMetric() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: deleteDeveloperRepositoryMetric() 
* 
*/ 
@Test
public void testDeleteDeveloperRepositoryMetric() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperList(Query query) 
* 
*/ 
@Test
public void testGetDeveloperList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperLevelList(Query query) 
* 
*/ 
@Test
public void testGetDeveloperLevelList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperCommitStandard(Query query) 
* 
*/ 
@Test
public void testGetDeveloperCommitStandard() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperCommitStandardWithLevel(Query query) 
* 
*/ 
@Test
public void testGetDeveloperCommitStandardWithLevel() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCommitStandardTrendChartIntegratedByProject(String projectIds, String since, String until, String token, String interval) 
* 
*/ 
@Test
public void testGetCommitStandardTrendChartIntegratedByProject() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getSingleProjectCommitStandardChart(Query query, ProjectPair projectPair) 
* 
*/ 
@Test
public void testGetSingleProjectCommitStandardChart() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: deleteProjectCommitStandardChart() 
* 
*/ 
@Test
public void testDeleteProjectCommitStandardChart() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCommitStandardDetailIntegratedByProject(String projectNameList, String repoUuidList, String committer, String token, int page, int ps, Boolean isValid) 
* 
*/ 
@Test
public void testGetCommitStandardDetailIntegratedByProject() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer, int beginIndex, int size, Boolean selectOrNot) 
* 
*/ 
@Test
public void testGetProjectValidCommitStandardDetailForRepoUuidListCommitterBeginIndexSizeSelectOrNot() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer) 
* 
*/ 
@Test
public void testGetProjectValidCommitStandardDetailForRepoUuidListCommitter() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCommitStandardCommitterList(String projectNameList, String repoUuidList, String token) 
* 
*/ 
@Test
public void testGetCommitStandardCommitterList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getHugeLocRemainedFile(String projectIds, String since, String until, String token, String interval) 
* 
*/ 
@Test
public void testGetHugeLocRemainedFile() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getAllProjectBigFileDetail(List<ProjectPair> projectPairList, String until) 
* 
*/ 
@Test
public void testGetAllProjectBigFileDetail() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: deleteProjectBigFileTrendChart() 
* 
*/ 
@Test
public void testDeleteProjectBigFileTrendChart() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getHugeLocRemainedDetail(String projectNameList, String repoUuidList, String token) 
* 
*/ 
@Test
public void testGetHugeLocRemainedDetail() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperDataCcn(String projectNameList, String developers, String token, String since, String until) 
* 
*/ 
@Test
public void testGetDeveloperDataCcn() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperDataCommitStandard(String projectNameList, String developers, String token, String since, String until) 
* 
*/ 
@Test
public void testGetDeveloperDataCommitStandard() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperDataWorkLoad(String projectNameList, String developers, String token, String since, String until) 
* 
*/ 
@Test
public void testGetDeveloperDataWorkLoad() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setMeasureDeveloperServiceImpl(MeasureDeveloperService measureDeveloperServiceImpl) 
* 
*/ 
@Test
public void testSetMeasureDeveloperServiceImpl() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setMethodMeasureAspect(MethodMeasureAspect methodMeasureAspect) 
* 
*/ 
@Test
public void testSetMethodMeasureAspect() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: clearCache() 
* 
*/ 
@Test
public void testClearCache() throws Exception { 
//TODO: Test goes here... 
} 


/** 
* 
* Method: getRepoWorkLoadLevel(int totalLoc, String repoUuid) 
* 
*/ 
@Test
public void testGetRepoWorkLoadLevel() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getRepoWorkLoadLevel", int.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getDeveloperEfficiency(Query query, String branch, int developerNumber) 
* 
*/ 
@Test
public void testGetDeveloperEfficiency() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getDeveloperEfficiency", Query.class, String.class, int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getDeveloperQuality(String repoUuid, String developer, String since, String until, String tool, String token, int developerNumber, int totalLOC) 
* 
*/ 
@Test
public void testGetDeveloperQuality() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getDeveloperQuality", String.class, String.class, String.class, String.class, String.class, String.class, int.class, int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getDeveloperCompetence(String repoUuid, String since, String until, String developer, int developerNumber, int developerAddStatement, int totalAddStatement, int developerValidLine, int totalValidLine) 
* 
*/ 
@Test
public void testGetDeveloperCompetence() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getDeveloperCompetence", String.class, String.class, String.class, String.class, int.class, int.class, int.class, int.class, int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getEfficiency(String repoUuid, String since, String until, String developer, String tool, String token) 
* 
*/ 
@Test
public void testGetEfficiency() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getEfficiency", String.class, String.class, String.class, String.class, String.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getQuality(String repoUuid, String developer, String since, String until, String tool, String token, int developerLOC, int developerCommitCount) 
* 
*/ 
@Test
public void testGetQuality() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getQuality", String.class, String.class, String.class, String.class, String.class, String.class, int.class, int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getContribution(String repoUuid, String since, String until, String developer, int developerLOC, String branch, int developerAddStatement, int developerChangeStatement, int developerValidStatement, int totalAddStatement, int totalValidStatement) 
* 
*/ 
@Test
public void testGetContribution() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getContribution", String.class, String.class, String.class, String.class, int.class, String.class, int.class, int.class, int.class, int.class, int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getTotalDeveloperMetrics(List<cn.edu.fudan.measureservice.portrait2.DeveloperMetrics> developerMetricsList, String developer, LocalDate firstCommitDate) 
* 
*/ 
@Test
public void testGetTotalDeveloperMetrics() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getTotalDeveloperMetrics", List<cn.edu.fudan.measureservice.portrait2.DeveloperMetrics>.class, String.class, LocalDate.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getLevel(double level) 
* 
*/ 
@Test
public void testGetLevel() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getLevel", double.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getRepoCommitStandardLevel(double commitStandard, String repoUuid) 
* 
*/ 
@Test
public void testGetRepoCommitStandardLevel() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("getRepoCommitStandardLevel", double.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: isInit(String repoUuid) 
* 
*/ 
@Test
public void testIsInit() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("isInit", String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
}

@Test
public void getDeveloperCommitStandardWithLevel() {
    String repoUuid = "a140dc46-50db-11eb-b7c3-394c0d058805";
    String developerName = "zwj";
    String since = "2021-03-01";
    String until = "2021-06-25";
    Query query = new Query(token,since,until,developerName, Collections.singletonList(repoUuid));
    DeveloperCommitStandard developerCommitStandard = measureDeveloperService.getDeveloperCommitStandardWithLevel(query);
    Assert.assertEquals(developerCommitStandard.getDeveloperValidCommitCount(),78);
    Assert.assertEquals(developerCommitStandard.getDeveloperJiraCommitCount(),54);
    Assert.assertEquals(developerCommitStandard.getDeveloperInvalidCommitCount(),24);
    Assert.assertEquals(0, Double.compare(developerCommitStandard.getCommitStandard(), 0.69));
}

    @Autowired
    public void setMeasureDeveloperService(MeasureDeveloperService measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }
}
