package cn.edu.fudan.issueservice.service.impl;


import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.IssueIgnoreDao;
import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import cn.edu.fudan.issueservice.service.IssueIgnoreService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Beethoven
 */
@Service
@Slf4j
public class IssueIgnoreServiceImpl implements IssueIgnoreService {

    private final Logger logger = LoggerFactory.getLogger(IssueIgnoreServiceImpl.class);

    private IssueIgnoreDao issueIgnoreDao;

    private IssueDao issueDao;

    @Override
    public String insertIssueIgnoreRecords(List<IgnoreRecord> ignoreRecords) {

        issueIgnoreDao.insertIssueIgnoreRecords(ignoreRecords);

        for (IgnoreRecord ignoreRecord : ignoreRecords){
            boolean updateIssueManualStatusSuccess = updateIssueManualStatus(ignoreRecord.getRepoUuid(), ignoreRecord.getIssueUuid(), ignoreRecord.getTag(), ignoreRecord.getType(), ignoreRecord.getTool());
            if(!updateIssueManualStatusSuccess){
                throw  new RuntimeException("update issue manual_status failed!");
            }
        }

        return "insert success!";
    }

    private boolean updateIssueManualStatus(String repoUuid, String issueUuid, String manualStatus, String issueType, String tool) {
        if (manualStatus == null || manualStatus.isEmpty()) {
            logger.error("manualStatus shouldn't be null!");
            return false;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        issueDao.updateIssueManualStatus(repoUuid, issueUuid, manualStatus, issueType, tool, df.format(new Date()));
        return true;
    }

    @Override
    public void deleteIssueIgnoreRecord(String tool, String issueUuid, String ignoreUuid) {
        //issueIgnoreDao.deleteIgnoreRecord()
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setIssueIgnoreDao(IssueIgnoreDao issueIgnoreDao) {
        this.issueIgnoreDao = issueIgnoreDao;
    }

}
