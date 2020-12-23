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
        scan.setUuid("cbdc9566-beaf-449e-8af1-cab5a3edf0d7");
        scan.setInvokeResult("2:1,3:1,4:1,5:1,6:1");
        scanDao.updateOneScan(scan);
        Scan newScan = scanDao.getScanByRepoId("3d4a99e0-3e23-11eb-8dca-4dbb5f7a5f33");
        Assert.assertEquals("调用结果不一致", "2:1,3:1,4:1,5:1,6:1", newScan.getInvokeResult());
    }


}
