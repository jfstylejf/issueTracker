package cn.edu.fudan.issueservice.domain.dto;

import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
public class IssueCountPo {

    private Date date;

    private int newIssueCount;

    private int eliminatedIssueCount;

    private int remainingIssueCount;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNewIssueCount() {
        return newIssueCount;
    }

    public void setNewIssueCount(int newIssueCount) {
        this.newIssueCount = newIssueCount;
    }

    public int getEliminatedIssueCount() {
        return eliminatedIssueCount;
    }

    public void setEliminatedIssueCount(int eliminatedIssueCount) {
        this.eliminatedIssueCount = eliminatedIssueCount;
    }

    public int getRemainingIssueCount() {
        return remainingIssueCount;
    }

    public void setRemainingIssueCount(int remainingIssueCount) {
        this.remainingIssueCount = remainingIssueCount;
    }

    @Override
    public String toString() {
        return "IssueCountPo{" +
                "date=" + date +
                ", newIssueCount=" + newIssueCount +
                ", eliminatedIssueCount=" + eliminatedIssueCount +
                ", remainingIssueCount=" + remainingIssueCount +
                '}';
    }
}
