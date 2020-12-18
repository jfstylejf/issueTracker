package cn.edu.fudan.scanservice.component.scan;

import cn.edu.fudan.scanservice.domain.dto.RepoResourceDTO;

public interface FirstScanCommitFilterStrategy {

    String filter(RepoResourceDTO repoResourceDTO, String repoId, String branch, Object argument) throws RuntimeException;

    String filterWithoutAggregationCommit(RepoResourceDTO repoResourceDTO, String repoId, String branch, Object argument);

}
