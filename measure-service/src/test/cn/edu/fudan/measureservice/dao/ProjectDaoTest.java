package cn.edu.fudan.measureservice.dao; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* ProjectDao Tester. 
* 
* @author wjzho 
* @since <pre>04/20/2021</pre> 
* @version 1.0 
*/ 
public class ProjectDaoTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
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
* Method: getDeveloperRepoInfoList(Query query) 
* 
*/ 
@Test
public void testGetDeveloperRepoInfoList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperDutyType(Set<String> developerList) 
* 
*/ 
@Test
public void testGetDeveloperDutyType() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: involvedRepoProcess(String repoUuidList, String token) 
* 
*/ 
@Test
public void testInvolvedRepoProcess() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getVisibleRepoInfoByToken(String token) 
* 
*/ 
@Test
public void testGetVisibleRepoInfoByToken() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectRepoList(String projectName, String token) 
* 
*/ 
@Test
public void testGetProjectRepoList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectInvolvedRepoInfo(String projectName, String token) 
* 
*/ 
@Test
public void testGetProjectInvolvedRepoInfo() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: insertProjectInfo(String token) 
* 
*/ 
@Test
public void testInsertProjectInfo() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getValidCommitMsg(Query query) 
* 
*/ 
@Test
public void testGetValidCommitMsg() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getRepoName(String repoUuid) 
* 
*/ 
@Test
public void testGetRepoName() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectName(String repoUuid) 
* 
*/ 
@Test
public void testGetProjectName() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperRankByCommitCount(Query query) 
* 
*/ 
@Test
public void testGetDeveloperRankByCommitCount() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperCommitCountsByDuration(Query query) 
* 
*/ 
@Test
public void testGetDeveloperCommitCountsByDuration() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: deleteRepoMsg(Query query) 
* 
*/ 
@Test
public void testDeleteRepoMsg() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getVisibleProjectByToken(String token) 
* 
*/ 
@Test
public void testGetVisibleProjectByToken() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: mergeBetweenRepo(List<String> source, List<String> target) 
* 
*/ 
@Test
public void testMergeBetweenRepo() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: mergeBetweenProject(List<String> source, List<String> target) 
* 
*/ 
@Test
public void testMergeBetweenProject() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: insertDeveloperLevel(List<DeveloperLevel> developerLevelList) 
* 
*/ 
@Test
public void testInsertDeveloperLevel() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperLevelList(List<String> developerList) 
* 
*/ 
@Test
public void testGetDeveloperLevelList() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getRepoInfoMap() 
* 
*/ 
@Test
public void testGetRepoInfoMap() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectInfo() 
* 
*/ 
@Test
public void testGetProjectInfo() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getToolName(String repoUuid) 
* 
*/ 
@Test
public void testGetToolName() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperFirstCommitDate(String developer, String since, String until, String repoUuid) 
* 
*/ 
@Test
public void testGetDeveloperFirstCommitDate() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectNameById(String projectIds) 
* 
*/ 
@Test
public void testGetProjectNameById() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectIdByName(String projectName) 
* 
*/ 
@Test
public void testGetProjectIdByName() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setRestInterface(RestInterfaceManager restInterface) 
* 
*/ 
@Test
public void testSetRestInterface() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setProjectMapper(ProjectMapper projectMapper) 
* 
*/ 
@Test
public void testSetProjectMapper() throws Exception { 
//TODO: Test goes here... 
} 
/**
* 
* Method: setMeasureMapper(MeasureMapper measureMapper) 
* 
*/ 
@Test
public void testSetMeasureMapper() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getRestInterface() 
* 
*/ 
@Test
public void testGetRestInterface() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getProjectMapper() 
* 
*/ 
@Test
public void testGetProjectMapper() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getMeasureMapper() 
* 
*/ 
@Test
public void testGetMeasureMapper() throws Exception { 
//TODO: Test goes here... 
} 


/** 
* 
* Method: transferRepoInfoToRepoList(List<RepoInfo> repoInfos) 
* 
*/ 
@Test
public void testTransferRepoInfoToRepoList() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ProjectDao.getClass().getMethod("transferRepoInfoToRepoList", List<RepoInfo>.class); 
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
* Method: getUserInfoByToken(String token) 
* 
*/ 
@Test
public void testGetUserInfoByToken() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ProjectDao.getClass().getMethod("getUserInfoByToken", String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

} 
