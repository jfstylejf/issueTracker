package cn.edu.fudan.measureservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProjectCommitStandardDetail
 * @Description: 项目总览页面 提交规范性明细
 * @Author wjzho
 * @Date 2021/4/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCommitStandardDetail {
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 提交commit id
     */
    private String commitId;
    /**
     * 提交时间
     */
    private String commitTime;
    /**
     * 提交人
     */
    private String committer;
    /**
     * 提交内容
     */
    private String message;
    /**
     * 是否是规范的提交
     */
    private boolean isValid;
}
