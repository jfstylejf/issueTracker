package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;

import java.util.Map;

public interface IssueRankService {

    //问题最多的文件排名
    Map<String, String> rankOfFileBaseIssueQuantity(String repoId, String commitId) ;

    //问题密度最大的文件排名
    Map<String, String> rankOfFileBaseDensity(String repoId, String commitId) ;

    //问题密度最大的开发者排名
    Map<String,Integer> rankOfDeveloper(RepoResourceDTO repoResourceDTO, String start, String end);

    //问题密度最大（小）的项目排名
    @Deprecated
    Map rankOfRepoBaseDensity(String token) ;
}
