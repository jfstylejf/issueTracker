package cn.edu.fudan.scanservice.dao;

import cn.edu.fudan.scanservice.ScanServiceApplicationTests;
import cn.edu.fudan.scanservice.domain.dbo.Scan;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ScanDaoTest extends ScanServiceApplicationTests {

    @Autowired
    private ScanDao scanDao;

    @Test
    public void testUpdateScan(){
        Scan scan = new Scan();
        scan.setUuid("test");
        scan.setRepoId("111");
        scan.setInvokeResult("2:1,3:1,4:1,5:1,6:1");
        scanDao.updateOneScan(scan);
        Scan newScan = scanDao.getScanByRepoId("111");
        Assert.assertEquals("调用结果不一致", "2:1,3:1,4:1,5:1,6:1", newScan.getInvokeResult());
    }


}
