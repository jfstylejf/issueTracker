package cn.edu.fudan.measureservice.domain.bo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * @ClassName: developerRecentNews
 * @Description: 开发者最新动态
 * @Author wjzho
 * @Date 2021/6/4
 */
@Data
@Builder
public class DeveloperRecentNews {
    /**
     * 开发者
     */
    private String developerName;
    /**
     * 提交时间
     */
    private LocalDate commitTime;
    /**
     * jira 内容
     */
    private Object jiraInfo;
    /**
     * 提交信息
     */
    private String message;
    /**
     * 提交库id
     */
    private String repoUuid;
    /**
     * 提交 id
     */
    private String commitId;

}
