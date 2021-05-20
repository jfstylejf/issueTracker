package cn.edu.fudan.issueservice.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author beethoven
 * @date 2021-05-20 21:42:01
 */

public class FileUtilTest {
    @Test
    public void findSrcDirTest() {
        String path = System.getProperty("user.dir");
        String srcDir = FileUtil.findSrcDir(path);
        Assert.assertEquals(path + "/src", srcDir);
    }
}
