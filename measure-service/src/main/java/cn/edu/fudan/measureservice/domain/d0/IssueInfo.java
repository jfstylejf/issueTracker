package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.IssueDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author wjzho
 */
@Slf4j
public class IssueInfo extends BaseData {


    private IssueDao issueDao;
    /**
     *  开发者个人引入规范类缺陷数
     */
    private int developerStandardIssueCount;
    /**
     *  团队引入规范类缺陷数
     */
    private int totalStandardIssueCount;
    /**
     *  开发者个人引入安全性缺陷数
     */
    private int developerSecurityIssueCount;
    /**
     *  开发者个人引入缺陷数
     */
    private int developerIssueCount;
    /**
     *  团队引入缺陷数
     */
    private int totalIssueCount;
    /**
     *  开发者个人解决缺陷数
     */
    private int developerSolvedSonarIssue;

    public IssueInfo(Query query) {
        super(query);
    }

    @Override
    public void dataInjection() {
        log.debug("{} : in repo : {} start to get IssueInfo",query.getDeveloper(),query.getRepoUuidList());
        /**
         *  key : getSolvedSonarIssue
         *  value {@link IssueDao}
         */
        developerSolvedSonarIssue = issueDao.getSolvedSonarIssue(query);
        /**
         * key : getIssueCountByConditions
         * value {@link IssueDao}
         */
        Map<String,Object> issueCountByConditionsMap = issueDao.getIssueCountByConditions(query);
        developerStandardIssueCount = (int) issueCountByConditionsMap.get("developerStandardIssueCount");
        totalStandardIssueCount = (int) issueCountByConditionsMap.get("totalStandardIssueCount");
        developerIssueCount = (int) issueCountByConditionsMap.get("developerIssueCount");
        totalIssueCount = (int) issueCountByConditionsMap.get("totalIssueCount");
        developerSecurityIssueCount = (int) issueCountByConditionsMap.get("developerSecurityIssueCount");

        log.info("get IssueInfo success");
    }

    @Autowired
    private void setIssueDao(IssueDao issueDao){
        this.issueDao = issueDao;
    }

}
