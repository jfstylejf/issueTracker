package cn.edu.fudan.issueservice.util;

import org.junit.Assert;
import org.junit.Test;

public class FileFilterTest {

    @Test
    public void fileFilterTest() {
        boolean test1 = FileFilter.fileFilter("TscanCode", "issueFirst.cpp");
        Assert.assertFalse(test1);

        boolean test2 = FileFilter.fileFilter("TscanCode", "issue.cpp");
        Assert.assertFalse(test2);

        boolean test3 = FileFilter.fileFilter("TscanCode", "test.cpp");
        Assert.assertTrue(test3);

        boolean test4 = FileFilter.fileFilter("TscanCode", "/.mvn/issueFirst.cpp");
        Assert.assertTrue(test4);

        boolean test5 = FileFilter.fileFilter("TscanCode", "/test/issueFirst.cpp");
        Assert.assertTrue(test5);

        boolean test6 = FileFilter.fileFilter("TscanCode", "issueFirst.cc");
        Assert.assertFalse(test6);

        boolean test7 = FileFilter.fileFilter("TscanCode", "issueFirst.h");
        Assert.assertFalse(test7);
    }
}
