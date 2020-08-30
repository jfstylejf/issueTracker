package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author zyh
 * @date 2020/4/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloneMeasure {

    private String uuid;
    private String repoId;
    private String commitId;

    private int newCloneLines;
    private int selfCloneLines;
    private int addLines;
    private int cloneLines;
    private Date commitTime;

    private Map<String, String> addCloneLocationMap;

    private Map<String, String> selfCloneLocationMap;

}
