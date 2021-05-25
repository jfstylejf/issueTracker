package cn.edu.fudan.scanservice.component;

import cn.edu.fudan.scanservice.ScanServiceApplicationTests;
import cn.edu.fudan.scanservice.component.scan.TimeAggregationStrategy;
import cn.edu.fudan.scanservice.domain.dto.RepoResourceDTO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;

public class TimeAggregationStrategyTest extends ScanServiceApplicationTests {

    @InjectMocks
    private TimeAggregationStrategy timeAggregationStrategy;

    @Test
    public void testUpdateScan(){
        RepoResourceDTO repoResourceDTO =  new RepoResourceDTO();
        String repoId = "a";

//        String firstCommit = timeAggregationStrategy.filterWithoutAggregationCommit(repoResourceDTO, repoId, "dev", 12);

//        Assert.assertEquals("调用结果不一致", "883af7cb6806ae3b50ba276fc3994c6bca7b0b50", firstCommit);
    }


}
