package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zyh
 * @date 2020/5/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitInfo {

    private String author;
    private String email;
    private int commit_counts;
    private int add;
    private int del;
    private int changed_files;

}
