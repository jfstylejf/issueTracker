package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * description: 记录项目信息
 *
 * @author fancying
 * create: 2020-11-19 18:20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private int id;
    private String projectName;
    private Date updateTime;
    private Date createTime;
    private String importAccountUuid;
    private Integer lifeStatus;
}