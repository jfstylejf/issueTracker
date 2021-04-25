package cn.edu.fudan.measureservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProjectBigFileDetail
 * @Description: 项目总览页面 超大文件数明细
 * @Author wjzho
 * @Date 2021/4/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectBigFileDetail {
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 代码库
     */
    private String repoUuid;
    /**
     * 库名称
     */
    private String repoName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 最近修改时间
     */
    private String currentModifyTime;
    /**
     * 最新行数
     */
    private int currentLines;
}
