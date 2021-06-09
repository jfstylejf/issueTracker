package cn.edu.fudan.measureservice.service; 

import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.Developer;
import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.dto.Query;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

/** 
* MeasureDeveloperService Tester. 
* 
* @author wjzho 
* @since <pre>04/15/2021</pre> 
* @version 1.0 
*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MeasureDeveloperServiceTest { 


   @InjectMocks
   @Resource
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
* Method: getDeveloperRecentNews(String repoUuid, String developer, String since, String until) 
* 
*/ 
@Test
public void testGetDeveloperRecentNews() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperPortrait(Query query, Map<String,List<DeveloperRepoInfo>> developerRepoInfos) 
* 
*/ 
@Test
public void testGetDeveloperPortrait() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperRepositoryMetric(DeveloperRepoInfo developerRepoInfo, Query query) 
* 
*/ 
@Test
public void testGetDeveloperRepositoryMetric() throws Exception {

} 

/** 
* 
* Method: getDeveloperList(Query redisQuery) 
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
* Method: getCommitStandard(Query query, List<String> developers) 
* 
*/ 
@Test
public void testSingleRepoGetCommitStandard() throws Exception {
    String token = "ec15d79e36e14dd258cfff3d48b73d35";
    String repoUuid0 = "a140dc46-50db-11eb-b7c3-394c0d058805";
    String repoUuid1 = "c28a14bc-8236-11eb-9988-b1d413682f00";
    String since = "2021-04-12";
    String until;

    // 单库单人测试
    until = "2021-04-16";
    Query query2 = new Query(token,since,until,"zwj", Collections.singletonList(repoUuid0));
    DeveloperCommitStandard developerCommitStandard = measureDeveloperService.getDeveloperCommitStandard(query2);
    Assert.assertEquals("开发者姓名错误",developerCommitStandard.getDeveloperName(),"zwj");
    Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),6);
    Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),0);
    Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),6);

    // 单库多人测试
    until = "2021-04-14";
    Query query1 = new Query(token,since,until,null, Collections.singletonList(repoUuid0));
    List<DeveloperCommitStandard> developerCommitStandardList1 = measureDeveloperService.getCommitStandard(query1);
    Assert.assertEquals("开发者人数有误",developerCommitStandardList1.size(),6);
    Map<String,DeveloperCommitStandard> map = new HashMap<>();
    for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList1) {
        map.put(developerCommitStandard.getDeveloperName(),developerCommitStandard);
    }
    Assert.assertTrue("开发者姓名获取错误",map.containsKey("zhangjingfu") && map.containsKey("Zrq-Q")  && map.containsKey("zwj") && map.containsKey("heyue") && map.containsKey("fancying"));
    if(map.containsKey("zhangjingfu")) {
        DeveloperCommitStandard developerCommitStandard = map.get("zhangjingfu");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),1);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),1);
    }
    if(map.containsKey("Zrq-Q")) {
        DeveloperCommitStandard developerCommitStandard = map.get("Zrq-Q");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),1);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),1);
    }
    if(map.containsKey("zwj")) {
        DeveloperCommitStandard developerCommitStandard = map.get("zwj");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),2);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),0);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),2);
    }
    if(map.containsKey("fancying")) {
        DeveloperCommitStandard developerCommitStandard = map.get("fancying");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),1);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),1);
    }
    if(map.containsKey("heyue")) {
        DeveloperCommitStandard developerCommitStandard = map.get("heyue");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),2);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),2);
    }

}

/**
 *
 * Method: getCommitStandard(Query query, List<String> developers)
 *
 */
public void  testMultipleRepoGetCommitStandard() throws Exception{
    String token = "ec15d79e36e14dd258cfff3d48b73d35";
    String repoUuid0 = "a140dc46-50db-11eb-b7c3-394c0d058805";
    String repoUuid1 = "4202370e-346e-11eb-8dca-4dbb5f7a5f33";
    String since = "2020-06-01";
    String until = "2020-06-15";

    // 多库单人测试
    Query query2 = new Query(token,since,until,"zhangjingfu", Arrays.asList(repoUuid0,repoUuid1));
    List<DeveloperCommitStandard> developerCommitStandardList2 = measureDeveloperService.getCommitStandard(query2,Collections.singletonList("zhangjingfu"));
    Assert.assertEquals("查询人数不对",developerCommitStandardList2.size(),1);
    DeveloperCommitStandard developerCommitStandard1 = developerCommitStandardList2.get(0);
    Assert.assertEquals("开发者姓名错误",developerCommitStandard1.getDeveloperName(),"zhangjingfu");
    Assert.assertEquals("开发者合法提交数错误",developerCommitStandard1.getDeveloperJiraCommitCount(),0);
    Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard1.getDeveloperInvalidCommitCount(),18);
    Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard1.getDeveloperValidCommitCount(),18);

    // 多库多人测试
    Query query1 = new Query(token,since,until,null,Arrays.asList(repoUuid0,repoUuid1));
    List<DeveloperCommitStandard> developerCommitStandardList1 = measureDeveloperService.getCommitStandard(query1,null);
    Assert.assertEquals("开发者人数有误",developerCommitStandardList1.size(),3);
    Map<String,DeveloperCommitStandard> map = new HashMap<>();
    for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList1) {
        map.put(developerCommitStandard.getDeveloperName(),developerCommitStandard);
    }
    Assert.assertTrue("开发者姓名获取错误",map.containsKey("zhangjingfu") && map.containsKey("yuping")  && map.containsKey("zhangyuhui"));
    if(map.containsKey("zhangjingfu")) {
        DeveloperCommitStandard developerCommitStandard = map.get("zhangjingfu");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),18);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),18);
    }
    if(map.containsKey("yuping")) {
        DeveloperCommitStandard developerCommitStandard = map.get("yuping");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),4);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),4);
    }
    if(map.containsKey("zhangyuhui")) {
        DeveloperCommitStandard developerCommitStandard = map.get("zhangyuhui");
        Assert.assertEquals("开发者合法提交数错误",developerCommitStandard.getDeveloperJiraCommitCount(),0);
        Assert.assertEquals("开发者不规范提交次数错误",developerCommitStandard.getDeveloperInvalidCommitCount(),4);
        Assert.assertEquals("开发者总提交次数有误（去Merge）",developerCommitStandard.getDeveloperValidCommitCount(),4);
    }

}



/** 
* 
* Method: getCommitStandardTrendChartIntegratedByProject(String projectIds, String since, String until, String token, String interval, boolean showDetail) 
* 
*/ 
@Test
public void testGetCommitStandardTrendChartIntegratedByProject() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCommitStandardDetailIntegratedByProject(String projectNameList, String repoUuidList, String since, String until, String token) 
* 
*/ 
@Test
public void testGetCommitStandardDetailIntegratedByProject() throws Exception { 
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
* Method: orderByDeveloperFirstCommitDate(List<DeveloperRepoInfo> developerRepoInfos) 
* 
*/ 
@Test
public void testOrderByDeveloperFirstCommitDate() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("orderByDeveloperFirstCommitDate", List<DeveloperRepoInfo>.class); 
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
* Method: dealWithDeveloperCommitStandardDetail(DeveloperCommitStandard developerCommitStandard, String projectName, int projectId, String repoUuid, String repoName) 
* 
*/ 
@Test
public void testDealWithDeveloperCommitStandardDetail() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MeasureDeveloperService.getClass().getMethod("dealWithDeveloperCommitStandardDetail", DeveloperCommitStandard.class, String.class, int.class, String.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
}



}
