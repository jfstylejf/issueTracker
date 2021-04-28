package cn.edu.fudan.measureservice.domain.dto;

import lombok.Data;

/**
 * @ClassName: ProjectPair
 * @Description: 项目名 和 项目Id 的匹配
 * @Author wjzho
 * @Date 2021/4/23
 */
@Data
public class ProjectPair {
    String projectName;
    Integer projectId;

    public ProjectPair(String projectName,int projectId) {
        this.projectName = projectName;
        this.projectId = projectId;
    }

}
