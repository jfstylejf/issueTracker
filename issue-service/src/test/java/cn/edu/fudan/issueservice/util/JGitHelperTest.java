package cn.edu.fudan.issueservice.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;

@RunWith(JUnit4.class)
public class JGitHelperTest {
    @InjectMocks
    private JGitHelper jGitHelper = new JGitHelper("C:\\Users\\Beethoven\\Desktop\\1\\IssueTracker-Master");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCommitListByBranchAndBeginCommitTest() throws Exception{
        String branch = "issue-shangqi106";
        String beginCommit = "cf5eba2615f16a91d1c81902bbc3a3fb69f7a612";
        List<String> commitListByBranchAndBeginCommit = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit);
    }

    @Test
    public void getScanCommitListByBranchAndBeginCommitTest() throws Exception{
        String branch = "issue-refactor";
        String beginCommit = "b7ff341b698431cb35186c899d774797e0bc562c";
        List<String> commitListByBranchAndBeginCommit = jGitHelper.getScanCommitListByBranchAndBeginCommit(branch, beginCommit);

        String branch2 = "issue-refactor";
        String beginCommit2 = "f05c148e7f5e5a82180494926af4262db40ebab1";
        commitListByBranchAndBeginCommit = jGitHelper.getScanCommitListByBranchAndBeginCommit(branch2, beginCommit2);

        String branch3 = "issue-shangqi106";
        String beginCommit3 = "cf5eba2615f16a91d1c81902bbc3a3fb69f7a612";
        commitListByBranchAndBeginCommit = jGitHelper.getScanCommitListByBranchAndBeginCommit(branch3, beginCommit3);
    }

}
