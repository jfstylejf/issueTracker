package cn.edu.fudan.projectmanager.domain;

import java.util.Date;

/**
 * @author zyh
 * @date 2020/3/16
 */
public class CompleteUpdate {

    private Date till_commit_time;

    private String repoId;

    public Date getTill_commit_time() {
        return till_commit_time;
    }

    public void setTill_commit_time(Date till_commit_time) {
        this.till_commit_time = till_commit_time;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }
}
