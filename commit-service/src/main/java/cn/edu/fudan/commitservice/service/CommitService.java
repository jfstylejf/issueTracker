package cn.edu.fudan.commitservice.service;


import cn.edu.fudan.commitservice.domain.Commit;

import java.util.Date;
import java.util.List;


public interface CommitService {


    List<Commit> getCommitList(String project_id);

    Date getCommitDate(String commit_id);

    Date getTillCommitDate(String repo_id);

    List<Commit> getScannedCommits(String project_id);
}
