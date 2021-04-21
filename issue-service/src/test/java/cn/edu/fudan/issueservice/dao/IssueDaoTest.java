package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.mapper.IssueMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IssueDaoTest extends IssueServiceApplicationTest {


    private IssueMapper issueMapper;

    @Autowired
    public void setIssueMapper(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    @Test
    public void updateIssueManualStatus() {
        Date currentDate = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = df.format(currentDate);
        issueMapper.updateIssueManualStatus(null, null, "Default", "\"@Deprecated\" code should not be used", "sonarqube", currentTime);
        System.out.println(1 + 1);
    }

}
