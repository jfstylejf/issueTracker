package cn.edu.fudan.measureservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoMeasure {

    private String uuid;
    private int files;
    /**
     * 有效代码行 （去除注释、空白行）
     */
    private int ncss;
    private int classes;
    private int functions;
    private double ccn;
    private int java_docs;
    private int java_doc_lines;
    private int single_comment_lines;
    private int multi_comment_lines;
    private String commit_id;
    private String commit_time;
    private String repo_id;
    private String developer_name;
    private String developer_email;
    private int add_lines;
    private int del_lines;
    private int add_comment_lines;
    private int del_comment_lines;
    private int changed_files;
    /**
     * 此条 commit 是否是 merge
     */
    private boolean is_merge;
    private String commit_message;
    private String first_parent_commit_id;
    private String second_parent_commit_id;
    /**
     * 提交信息是否包含 jira 单号， 0 for false, 1 for true
     */
    private int is_compliance;
    /**
     * 全部代码行（包含注释、空白行）
     */
    private int absoluteLines;

}
