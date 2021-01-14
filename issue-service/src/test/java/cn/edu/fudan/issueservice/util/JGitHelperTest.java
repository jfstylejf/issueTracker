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
    public void getScanCommitListByBranchAndBeginCommitTest() {
    }

}
