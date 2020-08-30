package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zyh
 * @date 2020/5/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloneInfo {

    private String uuid;
    private String repoId;
    private String commitId;
    private String filePath;
    private String newCloneLines;
    private String selfCloneLines;
    private String type;
}
