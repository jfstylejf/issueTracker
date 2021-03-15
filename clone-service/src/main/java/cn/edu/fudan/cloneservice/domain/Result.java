package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result {
    String groupId;
    String repoPath;
    String methodLoc;
    String snippetLoc;
}
