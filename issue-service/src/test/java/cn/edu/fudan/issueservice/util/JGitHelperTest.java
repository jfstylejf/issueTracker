package cn.edu.fudan.issueservice.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class JGitHelperTest {

    private final String repoPath = System.getProperty("user.dir") + "/src/test/dependency/repo/forTest";

    @InjectMocks
    private final JGitHelper jGitHelper = new JGitHelper(repoPath);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDiffFilePairTest() {

        String preCommit = "b45d74a4cb210a19e76e29bfd0cad3818bc45823";
        String curCommit = "947f14272fecf37428e756f502495cb81070e770";

        // 预期的结果
        List<String> expectedFiles = new ArrayList<>();
        expectedFiles.add(".idea/workspace.xml,.idea/workspace.xml");
        expectedFiles.add("src/main/java/issue/utli/jGitHelperTest/addFileTest1.java,");
        expectedFiles.add("src/main/java/issue/utli/jGitHelperTest/addFileTest2.java,src/main/java/issue/utli/jGitHelperTest/addFileTest2.java");
        expectedFiles.add(",src/main/java/issue/utli/jGitHelperTest/path1/renameAddFileTest4.java");
        expectedFiles.add("src/main/java/issue/utli/jGitHelperTest/path1/addFileTest4.java,");
        List<String> sortedExpectedFiles = expectedFiles.stream().sorted().collect(Collectors.toList());


        List<String> diffFiles = jGitHelper.getDiffFilePair(preCommit, curCommit, new HashMap<>(8), new HashMap<>(8));
//        String delimiter = ",";
//        List<String> preFiles = diffFiles.stream().filter(d -> !d.startsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(0)).collect(Collectors.toList());
//        List<String> curFiles = diffFiles.stream().filter(d -> !d.endsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(1)).collect(Collectors.toList());
        // 被测方法返回的结果
        List<String> sortedActualFiles = diffFiles.stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(sortedExpectedFiles, sortedActualFiles);

    }

    @Test
    public void getAllCommitParentsTest() {

        String curCommit = "4a5123a5816ffcf4d718907ceef5e9b7a2f4271e";
        List<String> expectedList = new ArrayList<>(4);
        expectedList.add("4a5123a5816ffcf4d718907ceef5e9b7a2f4271e");
        expectedList.add("f58b5c3e983cd17378449f41509e3cf6f9905901");
        expectedList.add("02c2a09c75f707b084a3ab054c8c1056cc39f73e");
        expectedList.add("0f12b892247642688e8393fb7395dc8accd34d99");
        List<String> actualList = jGitHelper.getAllCommitParents(curCommit);
        Assert.assertEquals(expectedList.size(), actualList.size());
        Assert.assertEquals(expectedList, actualList);

    }

}
