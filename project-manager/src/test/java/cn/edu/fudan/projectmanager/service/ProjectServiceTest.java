package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.ProjectManagerApplication;
import cn.edu.fudan.projectmanager.ProjectManagerApplicationTests;
import cn.edu.fudan.projectmanager.dao.ProjectDao;
import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.service.impl.ProjectServiceImpl;
import cn.edu.fudan.projectmanager.tool.MockTestConnection;
import cn.edu.fudan.projectmanager.tool.TestDataMaker;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;


@PrepareForTest({ProjectService.class,ProjectServiceImpl.class,ProjectDao.class})
public class ProjectServiceTest extends ProjectManagerApplicationTests{

    Project project;
    Project projectUpdate;
    Project projectInitial;
    private TestDataMaker testDataMaker;

    @Mock
    private ProjectDao projectDao;

    @Autowired
    @InjectMocks
    private ProjectService projectService = new ProjectServiceImpl();


    @Before
    public void setup() throws Exception {
        testDataMaker = new TestDataMaker();
        project = testDataMaker.projectMakerPro2();
        projectUpdate = testDataMaker.projectMakerPro1Update();
        projectInitial = testDataMaker.projectMakerPro1();
        MemberModifier.field(ProjectServiceImpl .class, "projectDao").set(projectService ,projectDao );
        System.out.println("finish mocking");
    }


    /*
        当传入的project的url地址为null
     */
    @Test
    public void addOneProjectTest1() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        try{
            project.setUrl(null);
            projectService.addOneProject("1",project);
        }catch(RuntimeException e){
            assertEquals(e.getMessage(),"please input the project url!");
        }
    }

    /*
        当传入的project的url地址为不是正确路径
     */
    @Test
    public void addOneProjectTest2() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        try{
            project.setUrl("https://github.co");
            String url = "https://github.co";
            projectService.addOneProject("1",project);
        }catch(RuntimeException e){
            assertEquals(e.getMessage(),"invalid url!");
        }
    }

    /*
        当传入的project的url地址不是guihub的项目
     */
    @Test
    public void addOneProjectTest3() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        try{
            project.setUrl("https://github.com/TheAlgorit");
            String url = "https://github.com/TheAlgorit";
            projectService.addOneProject("1",project);
        }catch(RuntimeException e){
            assertEquals(e.getMessage(),"invalid url!");
        }
    }

    /*
        当传入的project的url地址不是maven项目
     */
    @Test
    public void addOneProjectTest4() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        try{
            project.setUrl("https://github.com/TheAlgorithms/Java");
            String url = "https://github.com/TheAlgorithms/Java";
            projectService.addOneProject("1",project);
        }catch(RuntimeException e){
            assertEquals(e.getMessage(),"failed,this project is not maven project!");
        }
    }

    /*
        当传入的project的url地址过去已添加
     */
    @Test
    public void addOneProjectTest5() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        try{
            project.setUrl("https://github.com/spotify/docker-maven-plugin");
            PowerMockito.when(projectDao.hasBeenAdded(accountId,project.getUrl())).thenReturn(true);
            projectService.addOneProject("1",project);
        }catch(RuntimeException e){
            assertEquals(e.getMessage(),"The project has been added!");
        }
    }

    /*
        当传入的project的url地址正确并且未添加过
     */
    @Test
    public void addOneProjectTest6() throws Exception{
        String accountId = "1";
        doNothing().when(projectDao).addOneProject(project);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"send"));
        PowerMockito.when(projectDao.hasBeenAdded(accountId,project.getUrl())).thenReturn(false);
        projectService.addOneProject("1",project);
        verify(projectDao, Mockito.atLeast(1)).addOneProject(project);

    }


    @Test
    public void getProjectList() {
        List<Project> list = new ArrayList<Project>();
        list.add(project);
        list.add(projectUpdate);
        PowerMockito.when(projectDao.getProjectByAccountId("1")).thenReturn(list);

        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn("1");

        List<Project> listResult = (List<Project>)projectService.getProjectList("22222");
        Assert.assertEquals(2,listResult.size());
        Assert.assertEquals("pro2",listResult.get(0).getUuid());
        Assert.assertEquals("pro1",listResult.get(1).getUuid());



    }

    @Test
    public void getProjectByID() {
        PowerMockito.when(projectDao.getProjectByID("pro2")).thenReturn(project);
        Project projectResult = projectService.getProjectByID("pro2");
        Assert.assertEquals("pro2",projectResult.getUuid());
    }



    @Test
    public void updateProjectStatus() {
        PowerMockito.when(projectDao.getProjectByID("pro2")).thenReturn(projectInitial);
        String initialName = projectDao.getProjectByID("pro2").getName();
        PowerMockito.when(projectDao.getProjectByID("pro2")).thenReturn(projectUpdate);
        projectService.updateProjectStatus(project);
        String updateName = projectDao.getProjectByID("pro2").getName();
        Assert.assertNotEquals(initialName,updateName);
        Assert.assertEquals("UpdateData",updateName);
    }

    /*
        remove方法的测试之后完善
     */
    @Test
    @Ignore
    public void remove() {


    }

    @Test
    public void getProjectNameById() {
        PowerMockito.when(projectDao.getProjectNameById("pro1")).thenReturn("Java");
        String projectName = projectService.getProjectNameById("pro1");
        Assert.assertEquals("Java",projectName);
    }

    @Test
    public void getRepoPath() {
        PowerMockito.when(projectDao.getRepoPath("pro1")).thenReturn("path");
        String repoPath = projectService.getRepoPath("pro1");
        Assert.assertEquals("path",repoPath);
    }

    @Test
    public void getRepoId() {
        PowerMockito.when(projectDao.getRepoId("pro1")).thenReturn("RepoId");
        String repoId = projectService.getRepoId("pro1");
        Assert.assertEquals("RepoId",repoId);
    }

    @Test
    public void getProjectListByKeyWord() {
        String accountId = "1";
        String keyWord = "ja";
        List<Project> list = new ArrayList<Project>();
        list.add(project);
        list.add(projectInitial);
        MemberModifier.stub(MemberMatcher.method(ProjectServiceImpl.class,"getAccountId")).toReturn(accountId);
        PowerMockito.when(projectDao.getProjectByKeyWordAndAccountId(accountId,"ja")).thenReturn(list);

        List<Project> projects = (List<Project>) projectService.getProjectListByKeyWord("1", "ja");
        for (Project eachProject : projects) {
            Assert.assertEquals("Java",eachProject.getName());
        }
    }


    @Test
    public void getProjectIdList() {
        String accountId = "1";
        List<Project> list = new ArrayList<Project>();
        list.add(project);
        list.add(projectInitial);
        PowerMockito.when(projectDao.getProjectByAccountId(accountId)).thenReturn(list);

        List<String> projectIds = (List<String>) projectService.getProjectIdList("1");
        Assert.assertEquals(list.size(),projectIds.size());
        Assert.assertEquals(project.getUuid(),projectIds.get(0));
        Assert.assertEquals(projectInitial.getUuid(),projectIds.get(1));
    }


}