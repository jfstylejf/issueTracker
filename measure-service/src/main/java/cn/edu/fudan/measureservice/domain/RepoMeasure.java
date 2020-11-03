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
    private boolean is_merge;
    private String commit_message;
    private String first_parent_commit_id;
    private String second_parent_commit_id;


}
