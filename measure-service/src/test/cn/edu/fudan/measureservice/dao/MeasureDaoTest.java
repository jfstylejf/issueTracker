package cn.edu.fudan.measureservice.dao; 

import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileDetail;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 
* MeasureDao Tester. 
* 
* @author wjzho 
* @since <pre>04/22/2021</pre> 
* @version 1.0 
*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MeasureDaoTest {

    @InjectMocks
    @Resource
    private MeasureDao measureDao;

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getDeveloperWorkLoadData(Query query) 
* 
*/ 
@Test
public void testGetDeveloperWorkLoadData() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCommitCountsByDuration(Query query) 
* 
*/ 
@Test
public void testGetCommitCountsByDuration() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDeveloperRankByLoc(Query query) 
* 
*/ 
@Test
public void testGetDeveloperRankByLoc() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getMsgNumByRepo(Query query) 
* 
*/ 
@Test
public void testGetMsgNumByRepo() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCurrentBigFileInfo(List<String> repoUuidList, String until) 
* 
*/ 
@Test
public void testGetCurrentBigFileInfo() throws Exception { 
    String repoUuid0 = "6f1170ac-4102-11eb-b6ff-f9c372bb0fcb";
    String repoUuid1 = "52bb4f90-225d-11eb-8610-491d2d684483";
    String until = "2021-01-09";

    /**  单库测试，同时限定 until 时间查询到此之前的 超大文件数    */
    List<ProjectBigFileDetail> projectBigFileDetailList1 = measureDao.getCurrentBigFileInfo(Collections.singletonList(repoUuid0),until);
    Assert.assertEquals("超大文件数数量不对",projectBigFileDetailList1.size(),24);
    /**  多库测试，同时限定 until 时间查询到此之前的 超大文件数 */
    List<ProjectBigFileDetail> projectBigFileDetailList2 = measureDao.getCurrentBigFileInfo(Arrays.asList(repoUuid0,repoUuid1),until);
    Assert.assertEquals("超大文件数数量不对",projectBigFileDetailList2.size(),26);
} 



} 
